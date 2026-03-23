package fury.signalnest.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val msg: String, val code: Int = 0) : ApiResult<Nothing>()
}

class ApiService(private val baseUrl: String, private val token: String = "") {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val mime = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun req(path: String) = Request.Builder()
        .url("${baseUrl.trimEnd('/')}$path")
        .apply { if (token.isNotBlank()) header("Authorization", "Bearer $token") }

    suspend fun connect(password: String): ApiResult<ConnectResponse> {
        val body = json.encodeToString(ConnectRequest(password)).toRequestBody(mime)
        return call(req("/app/connect").post(body).build()) { json.decodeFromString<ConnectResponse>(it) }
    }

    suspend fun fetchEvents(): ApiResult<EventsResponse> =
        call(req("/app/events").get().build()) { json.decodeFromString<EventsResponse>(it) }

    suspend fun health(): ApiResult<Unit> =
        call(req("/health").get().build()) { Unit }

    // ── Phase 2: SNRL rule management ────────────────────────────────────────

    suspend fun getRules(): ApiResult<RulesResponse> =
        call(req("/app/rules").get().build()) { json.decodeFromString<RulesResponse>(it) }

    suspend fun createRule(name: String, text: String): ApiResult<RuleResponse> {
        val body = json.encodeToString(CreateRuleRequest(name, text)).toRequestBody(mime)
        return call(req("/app/rules").post(body).build()) { json.decodeFromString<RuleResponse>(it) }
    }

    suspend fun updateRule(id: String, req2: UpdateRuleRequest): ApiResult<RuleResponse> {
        val body = json.encodeToString(req2).toRequestBody(mime)
        val r = req("/app/rules/$id")
            .method("PATCH", body)
            .build()
        return call(r) { json.decodeFromString<RuleResponse>(it) }
    }

    suspend fun deleteRule(id: String): ApiResult<Unit> =
        call(req("/app/rules/$id").delete().build()) { Unit }

    suspend fun validateRule(text: String): ApiResult<ValidateResponse> {
        val body = json.encodeToString(ValidateRequest(text)).toRequestBody(mime)
        return call(req("/app/rules/validate").post(body).build()) { json.decodeFromString<ValidateResponse>(it) }
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private suspend fun <T> call(r: Request, decode: (String) -> T): ApiResult<T> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resp = client.newCall(r).execute()
                val raw  = resp.body?.string() ?: ""
                if (resp.isSuccessful) ApiResult.Success(decode(raw))
                else ApiResult.Failure(
                    runCatching { json.decodeFromString<ErrorResponse>(raw).error }
                        .getOrElse { "HTTP ${resp.code}" },
                    resp.code
                )
            }.getOrElse { ApiResult.Failure(it.message ?: "Network error") }
        }
}
