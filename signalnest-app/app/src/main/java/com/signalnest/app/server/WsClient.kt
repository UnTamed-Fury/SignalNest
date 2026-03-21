package com.signalnest.app.server

import android.util.Log
import com.signalnest.app.SignalNestApp
import com.signalnest.app.data.models.Event
import com.signalnest.app.notification.AppNotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

sealed class WsState {
    object Disconnected             : WsState()
    object Connecting               : WsState()
    data class Connected(val url: String) : WsState()
    data class Error(val msg: String)     : WsState()
}

class WsClient(
    private val scope: CoroutineScope,
    private val onState: (WsState) -> Unit,
) {
    private val TAG     = "WsClient"
    private var ws      : WebSocket? = null
    private val running = AtomicBoolean(false)
    private val notifId = AtomicInteger(4000)
    private var reconnJob: Job? = null

    private val client = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun connect(serverUrl: String, token: String) {
        if (!running.compareAndSet(false, true)) return
        val wsUrl = serverUrl.trimEnd('/')
            .replace("https://", "wss://")
            .replace("http://", "ws://") + "/ws?token=$token"

        onState(WsState.Connecting)
        ws = client.newWebSocket(Request.Builder().url(wsUrl).build(), object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, resp: Response) {
                Log.i(TAG, "Connected")
                onState(WsState.Connected(serverUrl))
            }
            override fun onMessage(ws: WebSocket, text: String) {
                scope.launch { handleMsg(text, serverUrl, token) }
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                onState(WsState.Disconnected)
                scheduleReconnect(serverUrl, token)
            }
            override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) {
                Log.e(TAG, "Failure: ${t.message}")
                onState(WsState.Error(t.message ?: "Connection failed"))
                running.set(false)
                scheduleReconnect(serverUrl, token)
            }
        })
    }

    private suspend fun handleMsg(text: String, serverUrl: String, token: String) {
        try {
            val obj  = json.parseToJsonElement(text).jsonObject
            val type = obj["type"]?.jsonPrimitive?.content ?: return
            val app  = SignalNestApp.instance
            val prefs = app.prefs
            val nm    = AppNotificationManager(app)

            when (type) {
                "event" -> {
                    val data  = obj["data"]?.jsonObject ?: return
                    val event = parseEvent(data)
                    app.db.events().insert(event)
                    val silent = event.category == "silent"
                    val sound  = prefs.notifSound.first()
                    val vib    = prefs.notifVib.first()
                    nm.showEvent(notifId.getAndIncrement(), event.title, event.body, silent, sound, vib)
                }
                "events" -> {
                    val list = obj["data"]?.jsonArray ?: return
                    val events = list.mapNotNull {
                        runCatching { parseEvent(it.jsonObject) }.getOrNull()
                    }
                    if (events.isNotEmpty()) app.db.events().insertAll(events)
                }
                "ping" -> { /* server keepalive, no-op */ }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleMsg error: ${e.message}")
        }
    }

    private fun parseEvent(obj: JsonObject): Event {
        fun str(vararg keys: String) = keys.firstNotNullOfOrNull {
            obj[it]?.jsonPrimitive?.content?.takeIf { v -> v.isNotBlank() }
        }
        return Event(
            id         = str("id") ?: java.util.UUID.randomUUID().toString(),
            title      = str("title", "summary", "subject") ?: "Event",
            body       = str("body", "message", "description", "msg") ?: "",
            source     = str("source", "service") ?: "remote",
            category   = str("category") ?: "normal",
            group      = str("group") ?: "default",
            rawPayload = obj.toString(),
            channel    = "remote",
        )
    }

    private fun scheduleReconnect(url: String, token: String) {
        if (!running.get()) return
        reconnJob?.cancel()
        reconnJob = scope.launch {
            delay(5_000)
            if (running.get()) { running.set(false); connect(url, token) }
        }
    }

    fun disconnect() {
        running.set(false)
        reconnJob?.cancel()
        ws?.close(1000, "User disconnected")
        ws = null
        onState(WsState.Disconnected)
    }
}
