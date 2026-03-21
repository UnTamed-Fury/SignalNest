package com.signalnest.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("signalnest_prefs")

class PreferencesManager(private val ctx: Context) {

    companion object {
        // Onboarding
        val K_ONBOARDED     = booleanPreferencesKey("onboarded")
        // Connection
        val K_SERVER_URL    = stringPreferencesKey("server_url")
        val K_PASSWORD      = stringPreferencesKey("password")       // app-level password
        val K_WS_TOKEN      = stringPreferencesKey("ws_token")       // token returned by server
        // Appearance
        val K_THEME         = stringPreferencesKey("theme")          // SYSTEM | LIGHT | DARK
        val K_AMOLED        = booleanPreferencesKey("amoled")
        // Notifications
        val K_NOTIF_SOUND   = booleanPreferencesKey("notif_sound")
        val K_NOTIF_VIB     = booleanPreferencesKey("notif_vibrate")
        val K_NOTIF_SILENT_BADGE = booleanPreferencesKey("notif_silent_badge")
        // Feed
        val K_MAX_EVENTS    = intPreferencesKey("max_events")
        val K_DEFAULT_GROUP = stringPreferencesKey("default_group")
    }

    val onboarded:   Flow<Boolean> = ctx.dataStore.data.map { it[K_ONBOARDED]     ?: false }
    val serverUrl:   Flow<String>  = ctx.dataStore.data.map { it[K_SERVER_URL]    ?: "" }
    val password:    Flow<String>  = ctx.dataStore.data.map { it[K_PASSWORD]      ?: "" }
    val wsToken:     Flow<String>  = ctx.dataStore.data.map { it[K_WS_TOKEN]      ?: "" }
    val theme:       Flow<String>  = ctx.dataStore.data.map { it[K_THEME]         ?: "SYSTEM" }
    val amoled:      Flow<Boolean> = ctx.dataStore.data.map { it[K_AMOLED]        ?: false }
    val notifSound:  Flow<Boolean> = ctx.dataStore.data.map { it[K_NOTIF_SOUND]   ?: true }
    val notifVib:    Flow<Boolean> = ctx.dataStore.data.map { it[K_NOTIF_VIB]     ?: true }
    val silentBadge: Flow<Boolean> = ctx.dataStore.data.map { it[K_NOTIF_SILENT_BADGE] ?: true }
    val maxEvents:   Flow<Int>     = ctx.dataStore.data.map { it[K_MAX_EVENTS]    ?: 500 }

    private suspend fun edit(block: MutablePreferences.() -> Unit) =
        ctx.dataStore.edit(block)

    suspend fun completeOnboarding(url: String, password: String, token: String) = edit {
        this[K_ONBOARDED]  = true
        this[K_SERVER_URL] = url
        this[K_PASSWORD]   = password
        this[K_WS_TOKEN]   = token
    }
    suspend fun setServerUrl(v: String)  = edit { this[K_SERVER_URL]  = v }
    suspend fun setPassword(v: String)   = edit { this[K_PASSWORD]    = v }
    suspend fun setWsToken(v: String)    = edit { this[K_WS_TOKEN]    = v }
    suspend fun setTheme(v: String)      = edit { this[K_THEME]       = v }
    suspend fun setAmoled(v: Boolean)    = edit { this[K_AMOLED]      = v }
    suspend fun setNotifSound(v: Boolean)= edit { this[K_NOTIF_SOUND] = v }
    suspend fun setNotifVib(v: Boolean)  = edit { this[K_NOTIF_VIB]   = v }
    suspend fun setSilentBadge(v: Boolean)=edit { this[K_NOTIF_SILENT_BADGE] = v }
    suspend fun setMaxEvents(v: Int)     = edit { this[K_MAX_EVENTS]  = v }
    suspend fun resetOnboarding()        = edit {
        remove(K_ONBOARDED); remove(K_SERVER_URL)
        remove(K_PASSWORD);  remove(K_WS_TOKEN)
    }
}
