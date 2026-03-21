package com.signalnest.app.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Auth ──────────────────────────────────────────────────────────────────────
@Serializable data class ConnectRequest(val password: String)
@Serializable data class ConnectResponse(val token: String, val ok: Boolean = true)

// ── Event from server ─────────────────────────────────────────────────────────
@Serializable
data class RemoteEvent(
    val id: String,
    val title: String,
    val body: String,
    val source: String = "custom",
    val category: String = "normal",
    val group: String   = "default",
    val rawPayload: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

@Serializable data class EventsResponse(val events: List<RemoteEvent>)

// ── WebSocket messages ────────────────────────────────────────────────────────
@Serializable
data class WsIncoming(
    val type: String,
    val data: JsonElement? = null,
)

// ── Errors ────────────────────────────────────────────────────────────────────
@Serializable data class ErrorResponse(val error: String)
