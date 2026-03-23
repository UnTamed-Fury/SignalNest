package fury.signalnest.app.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fury.signalnest.app.SignalNestApp
import fury.signalnest.app.data.ExportManager
import fury.signalnest.app.data.ImportResult
import fury.signalnest.app.data.models.*
import fury.signalnest.app.network.*
import fury.signalnest.app.server.ConnectionService
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

    fun markRead(id: String)           = viewModelScope.launch { db.events().markRead(id) }
    fun markAllRead()                  = viewModelScope.launch { db.events().markAllRead() }
    fun pin(id: String, v: Boolean)    = viewModelScope.launch { db.events().setPin(id, v) }
    fun delete(e: Event)               = viewModelScope.launch { db.events().delete(e) }
    fun clearAll()                     = viewModelScope.launch { db.events().deleteAll() }
}

// ── Search (Phase 2) ──────────────────────────────────────────────────────────
class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.events()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<Event>> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else dao.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun clear()             { _query.value = "" }
    fun markRead(id: String) = viewModelScope.launch { dao.markRead(id) }
    fun delete(e: Event)     = viewModelScope.launch { dao.delete(e) }
}

// ── SNRL Rules (Phase 2) ──────────────────────────────────────────────────────
sealed class RuleOp {
    object Idle : RuleOp()
    object Loading : RuleOp()
    data class Error(val msg: String) : RuleOp()
    object Done : RuleOp()
}

class RulesViewModel(app: Application) : AndroidViewModel(app) {
    private val sApp  = app as SignalNestApp
    private val dao   = sApp.db.rules()
    private val prefs = sApp.prefs

    val rules = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _op = MutableStateFlow<RuleOp>(RuleOp.Idle)
    val op: StateFlow<RuleOp> = _op.asStateFlow()

    private val _validateResult = MutableStateFlow<ValidateResponse?>(null)
    val validateResult: StateFlow<ValidateResponse?> = _validateResult.asStateFlow()

    private suspend fun api(): ApiService {
        val url   = prefs.serverUrl.first()
        val token = prefs.wsToken.first()
        return ApiService(url, token)
    }

    /** Sync rules from server → local DB */
    fun syncFromServer() = viewModelScope.launch {
        _op.value = RuleOp.Loading
        when (val r = api().getRules()) {
            is ApiResult.Success -> {
                val remoteRules = r.data.rules.mapIndexed { i, rr ->
                    SnrlRule(
                        id        = rr.id,
                        name      = rr.name,
                        text      = rr.text,
                        enabled   = rr.enabled,
                        order     = rr.order,
                        updatedAt = System.currentTimeMillis(),
                    )
                }
                dao.deleteAll()
                dao.insertAll(remoteRules)
                _op.value = RuleOp.Done
            }
            is ApiResult.Failure -> _op.value = RuleOp.Error(r.msg)
        }
    }

    fun createRule(name: String, text: String) = viewModelScope.launch {
        _op.value = RuleOp.Loading
        when (val r = api().createRule(name, text)) {
            is ApiResult.Success -> {
                val rr = r.data.rule
                dao.insert(SnrlRule(id = rr.id, name = rr.name, text = rr.text, order = rr.order))
                _op.value = RuleOp.Done
            }
            is ApiResult.Failure -> _op.value = RuleOp.Error(r.msg)
        }
    }

    fun toggleEnabled(rule: SnrlRule) = viewModelScope.launch {
        val newState = !rule.enabled
        _op.value = RuleOp.Loading
        when (val r = api().updateRule(rule.id, UpdateRuleRequest(enabled = newState))) {
            is ApiResult.Success -> {
                dao.update(rule.copy(enabled = newState, updatedAt = System.currentTimeMillis()))
                _op.value = RuleOp.Done
            }
            is ApiResult.Failure -> _op.value = RuleOp.Error(r.msg)
        }
    }

    fun updateRule(rule: SnrlRule, newName: String, newText: String) = viewModelScope.launch {
        _op.value = RuleOp.Loading
        when (val r = api().updateRule(rule.id, UpdateRuleRequest(name = newName, text = newText))) {
            is ApiResult.Success -> {
                dao.update(rule.copy(name = newName, text = newText, updatedAt = System.currentTimeMillis()))
                _op.value = RuleOp.Done
            }
            is ApiResult.Failure -> _op.value = RuleOp.Error(r.msg)
        }
    }

    fun deleteRule(rule: SnrlRule) = viewModelScope.launch {
        _op.value = RuleOp.Loading
        when (val r = api().deleteRule(rule.id)) {
            is ApiResult.Success -> { dao.delete(rule); _op.value = RuleOp.Done }
            is ApiResult.Failure -> _op.value = RuleOp.Error(r.msg)
        }
    }

    fun validateRule(text: String) = viewModelScope.launch {
        when (val r = api().validateRule(text)) {
            is ApiResult.Success -> _validateResult.value = r.data
            is ApiResult.Failure -> _validateResult.value = ValidateResponse(ok = false, error = r.msg)
        }
    }

    fun clearValidateResult() { _validateResult.value = null }
    fun clearOp()             { _op.value = RuleOp.Idle }
}

// ── Export / Import (Phase 2) ─────────────────────────────────────────────────
sealed class ExportState {
    object Idle : ExportState()
    object Working : ExportState()
    data class Done(val result: String) : ExportState()
    data class Error(val msg: String) : ExportState()
}

class ExportViewModel(app: Application) : AndroidViewModel(app) {
    private val mgr = ExportManager((app as SignalNestApp).db)
    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> = _state.asStateFlow()

    fun export(ctx: Context, uri: Uri) = viewModelScope.launch {
        _state.value = ExportState.Working
        runCatching { mgr.exportToUri(ctx, uri) }
            .onSuccess { _state.value = ExportState.Done("Export saved successfully") }
            .onFailure { _state.value = ExportState.Error(it.message ?: "Export failed") }
    }

    fun import(ctx: Context, uri: Uri) = viewModelScope.launch {
        _state.value = ExportState.Working
        runCatching { mgr.importFromUri(ctx, uri) }
            .onSuccess { _state.value = ExportState.Done(it.toString()) }
            .onFailure { _state.value = ExportState.Error(it.message ?: "Import failed") }
    }

    fun reset() { _state.value = ExportState.Idle }
}

// ── Notes ─────────────────────────────────────────────────────────────────────
class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.notes()
    val notes = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(title: String, content: String, colorIdx: Int) = viewModelScope.launch {
        dao.insert(Note(title = title, content = content, colorIndex = colorIdx))
    }
    fun update(n: Note)    = viewModelScope.launch { dao.update(n.copy(updatedAt = System.currentTimeMillis())) }
    fun delete(n: Note)    = viewModelScope.launch { dao.delete(n) }
    fun togglePin(n: Note) = viewModelScope.launch { dao.update(n.copy(isPinned = !n.isPinned)) }
}

// ── Todos ─────────────────────────────────────────────────────────────────────
class TodosViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.todos()
    val todos = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(title: String, desc: String, dueAt: Long?, cronExpr: String, priority: Int) = viewModelScope.launch {
        dao.insert(Todo(title = title, description = desc, dueAt = dueAt, cronExpr = cronExpr, priority = priority))
    }
    fun update(t: Todo)                  = viewModelScope.launch { dao.update(t) }
    fun delete(t: Todo)                  = viewModelScope.launch { dao.delete(t) }
    fun setDone(id: Long, done: Boolean) = viewModelScope.launch { dao.setDone(id, done) }
}

// ── RSS ───────────────────────────────────────────────────────────────────────
class RssViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as SignalNestApp).db.feeds()
    val feeds = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(title: String, url: String, intervalMins: Int, silent: Boolean) = viewModelScope.launch {
        dao.insert(RssFeed(title = title, url = url, intervalMinutes = intervalMins, notifySilent = silent))
    }
    fun update(f: RssFeed)        = viewModelScope.launch { dao.update(f) }
    fun delete(f: RssFeed)        = viewModelScope.launch { dao.delete(f) }
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

    fun setTheme(v: String)       = viewModelScope.launch { prefs.setTheme(v) }
    fun setAmoled(v: Boolean)     = viewModelScope.launch { prefs.setAmoled(v) }
    fun setNotifSound(v: Boolean) = viewModelScope.launch { prefs.setNotifSound(v) }
    fun setNotifVib(v: Boolean)   = viewModelScope.launch { prefs.setNotifVib(v) }
    fun setMaxEvents(v: Int)      = viewModelScope.launch { prefs.setMaxEvents(v) }
    fun resetOnboarding(ctx: Context) = viewModelScope.launch {
        prefs.resetOnboarding()
        ConnectionService.stop(ctx)
    }
}
