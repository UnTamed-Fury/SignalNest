package fury.signalnest.app.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable data class ConnectRequest(val password: String)
@Serializable data class ConnectResponse(val token: String, val ok: Boolean = true)

@Serializable
data class RemoteEvent(
    val id: String,
    val title: String,
    val body: String,
    val source: String   = "custom",
    val category: String = "normal",
    val group: String    = "default",
    val rawPayload: String = "",
    val timestamp: Long  = System.currentTimeMillis(),
)
@Serializable data class EventsResponse(val events: List<RemoteEvent>)

@Serializable data class WsIncoming(val type: String, val data: JsonElement? = null)
@Serializable data class ErrorResponse(val error: String)

// ── Phase 2: SNRL rule models ─────────────────────────────────────────────────
@Serializable
data class RemoteRule(
    val id: String,
    val name: String,
    val text: String,
    val enabled: Boolean = true,
    val order: Int       = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
)
@Serializable data class RulesResponse(val rules: List<RemoteRule>)
@Serializable data class RuleResponse(val rule: RemoteRule)
@Serializable data class CreateRuleRequest(val name: String, val text: String)
@Serializable data class UpdateRuleRequest(
    val name: String?    = null,
    val text: String?    = null,
    val enabled: Boolean?= null,
    val order: Int?      = null,
)
@Serializable data class ValidateRequest(val text: String)
@Serializable data class ValidateResponse(
    val ok: Boolean,
    val error: String?    = null,
    val warnings: List<String> = emptyList(),
)
