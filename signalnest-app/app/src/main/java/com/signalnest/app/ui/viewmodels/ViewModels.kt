package com.signalnest.app.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.signalnest.app.SignalNestApp
import com.signalnest.app.data.PreferencesManager
import com.signalnest.app.data.models.*
import com.signalnest.app.network.ApiResult
import com.signalnest.app.network.ApiService
import com.signalnest.app.server.ConnectionService
import com.signalnest.app.utils.NetworkUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Onboarding ────────────────────────────────────────────────────────────────

sealed class OnboardState {
    object Idle    : OnboardState()
    object Loading : OnboardState()
    data class Error(val msg: String) : OnboardState()
    object Done    : OnboardState()
}

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as SignalNestApp).prefs

    private val _state = MutableStateFlow<OnboardState>(OnboardState.Idle)
    val state: StateFlow<OnboardState> = _state.asStateFlow()

    fun connect(url: String, password: String, ctx: Context) = viewModelScope.launch {
        _state.value = OnboardState.Loading
        when (val r = ApiService(url).connect(password)) {
            is ApiResult.Success -> {
                prefs.completeOnboarding(url, password, r.data.token)
                ConnectionService.start(ctx)
                _state.value = OnboardState.Done
            }
            is ApiResult.Failure -> _state.value = OnboardState.Error(r.msg)
        }
    }
}

// ── Feed ──────────────────────────────────────────────────────────────────────

class FeedViewModel(app: Application) : AndroidViewModel(app) {
    private val db    = (app as SignalNestApp).db
    private val prefs = (app as SignalNestApp).prefs

    val events      = db.events().getRecent(500).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val unreadCount = db.events().unreadCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val groups      = db.events().allGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val serverUrl   = prefs.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun markRead(id: String)    = viewModelScope.launch { db.events().markRead(id) }
    fun markAllRead()           = viewModelScope.launch { db.events().markAllRead() }
    fun pin(id: String, v: Boolean) = viewModelScope.launch { db.events().setPin(id, v) }
    fun delete(e: Event)        = viewModelScope.launch { db.events().delete(e) }
    fun clearAll()              = viewModelScope.launch { db.events().deleteAll() }
}

// ── Notes ─────────────────────────────────────────────────────────────────────

class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.notes()
    val notes = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(title: String, content: String, colorIdx: Int) = viewModelScope.launch {
        dao.insert(Note(title = title, content = content, colorIndex = colorIdx))
    }
    fun update(n: Note)   = viewModelScope.launch { dao.update(n.copy(updatedAt = System.currentTimeMillis())) }
    fun delete(n: Note)   = viewModelScope.launch { dao.delete(n) }
    fun togglePin(n: Note)= viewModelScope.launch { dao.update(n.copy(isPinned = !n.isPinned)) }
}

// ── Todos ─────────────────────────────────────────────────────────────────────

class TodosViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.todos()
    val todos = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(title: String, desc: String, dueAt: Long?, cronExpr: String, priority: Int) = viewModelScope.launch {
        dao.insert(Todo(title = title, description = desc, dueAt = dueAt, cronExpr = cronExpr, priority = priority))
    }
    fun update(t: Todo)         = viewModelScope.launch { dao.update(t) }
    fun delete(t: Todo)         = viewModelScope.launch { dao.delete(t) }
    fun setDone(id: Long, done: Boolean) = viewModelScope.launch { dao.setDone(id, done) }
}

// ── RSS ───────────────────────────────────────────────────────────────────────

class RssViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.feeds()
    val feeds = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(title: String, url: String, intervalMins: Int, silent: Boolean) = viewModelScope.launch {
        dao.insert(RssFeed(title = title, url = url, intervalMinutes = intervalMins, notifySilent = silent))
    }
    fun update(f: RssFeed) = viewModelScope.launch { dao.update(f) }
    fun delete(f: RssFeed) = viewModelScope.launch { dao.delete(f) }
    fun toggleEnabled(f: RssFeed) = viewModelScope.launch { dao.update(f.copy(isEnabled = !f.isEnabled)) }
}

// ── Settings ──────────────────────────────────────────────────────────────────

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as SignalNestApp).prefs

    val onboarded  = prefs.onboarded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val serverUrl  = prefs.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val theme      = prefs.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")
    val amoled     = prefs.amoled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val notifSound = prefs.notifSound.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val notifVib   = prefs.notifVib.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val maxEvents  = prefs.maxEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 500)

    fun setTheme(v: String)      = viewModelScope.launch { prefs.setTheme(v) }
    fun setAmoled(v: Boolean)    = viewModelScope.launch { prefs.setAmoled(v) }
    fun setNotifSound(v: Boolean)= viewModelScope.launch { prefs.setNotifSound(v) }
    fun setNotifVib(v: Boolean)  = viewModelScope.launch { prefs.setNotifVib(v) }
    fun setMaxEvents(v: Int)     = viewModelScope.launch { prefs.setMaxEvents(v) }
    fun resetOnboarding(ctx: Context) = viewModelScope.launch {
        prefs.resetOnboarding()
        ConnectionService.stop(ctx)
    }
}
