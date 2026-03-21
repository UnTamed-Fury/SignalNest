package com.signalnest.app.network

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

    // FIX: non-inline functions cannot call inline reified functions.
    // Solution: call execute() directly with the full request — no wrapper functions.

    suspend fun connect(password: String): ApiResult<ConnectResponse> {
        val body = json.encodeToString(ConnectRequest(password)).toRequestBody(mime)
        val r = req("/app/connect").post(body).build()
        return withContext(Dispatchers.IO) {
            runCatching {
                val resp = client.newCall(r).execute()
                val raw  = resp.body?.string() ?: ""
                if (resp.isSuccessful) ApiResult.Success(json.decodeFromString<ConnectResponse>(raw))
                else ApiResult.Failure(runCatching { json.decodeFromString<ErrorResponse>(raw).error }.getOrElse { "HTTP ${resp.code}" }, resp.code)
            }.getOrElse { ApiResult.Failure(it.message ?: "Network error") }
        }
    }

    suspend fun fetchEvents(): ApiResult<EventsResponse> {
        val r = req("/app/events").get().build()
        return withContext(Dispatchers.IO) {
            runCatching {
                val resp = client.newCall(r).execute()
                val raw  = resp.body?.string() ?: ""
                if (resp.isSuccessful) ApiResult.Success(json.decodeFromString<EventsResponse>(raw))
                else ApiResult.Failure("HTTP ${resp.code}", resp.code)
            }.getOrElse { ApiResult.Failure(it.message ?: "Network error") }
        }
    }

    suspend fun health(): ApiResult<Unit> {
        val r = req("/health").get().build()
        return withContext(Dispatchers.IO) {
            runCatching {
                val resp = client.newCall(r).execute()
                if (resp.isSuccessful) ApiResult.Success(Unit)
                else ApiResult.Failure("HTTP ${resp.code}", resp.code)
            }.getOrElse { ApiResult.Failure(it.message ?: "Network error") }
        }
    }
}
