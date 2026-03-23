package fury.signalnest.app.server

import android.util.Log
import fury.signalnest.app.SignalNestApp
import fury.signalnest.app.data.models.Event
import fury.signalnest.app.notification.AppNotificationManager
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class LanServer(port: Int, private val scope: CoroutineScope) : NanoHTTPD(port) {

    private val TAG     = "LanServer"
    private val notifId = AtomicInteger(5000)
    private val json    = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override fun serve(session: IHTTPSession): Response = when {
        session.method == Method.GET  && session.uri == "/health"  -> health()
        session.method == Method.POST && session.uri == "/webhook" -> webhook(session)
        else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
    }

    private fun health() = newFixedLengthResponse(
        Response.Status.OK, "application/json", """{"status":"ok","app":"SignalNest"}"""
    )

    private fun webhook(session: IHTTPSession): Response {
        return try {
            val len  = session.headers["content-length"]?.toIntOrNull() ?: 0
            val body = if (len > 0) {
                val buf = ByteArray(len); session.inputStream.read(buf); String(buf)
            } else session.inputStream.bufferedReader().readText()

            val ip = session.remoteIpAddress ?: "unknown"

            scope.launch {
                val prefs = SignalNestApp.instance.prefs

                val jsonObj = runCatching {
                    json.parseToJsonElement(body).jsonObject
                }.getOrElse {
                    buildJsonObject { put("message", body.take(300)) }
                }

                fun str(vararg keys: String) = keys.firstNotNullOfOrNull {
                    jsonObj[it]?.jsonPrimitive?.content?.takeIf { v -> v.isNotBlank() }
                }

                val event = Event(
                    id         = str("id") ?: UUID.randomUUID().toString(),
                    title      = str("title", "summary", "subject") ?: "LAN Webhook",
                    body       = str("body", "message", "description", "msg") ?: body.take(300),
                    source     = str("source", "service") ?: ip,
                    category   = str("category") ?: "normal",
                    group      = str("group") ?: "default",
                    rawPayload = body,
                    channel    = "lan",
                )

                val app   = SignalNestApp.instance
                val sound = prefs.notifSound.first()
                val vib   = prefs.notifVib.first()
                val silent = event.category == "silent"

                app.db.events().insert(event)
                AppNotificationManager(app).showEvent(
                    notifId.getAndIncrement(), event.title, event.body, silent, sound, vib
                )
                Log.d(TAG, "LAN webhook processed: ${event.title}")
            }

            newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"accepted"}""")
        } catch (e: Exception) {
            Log.e(TAG, "Webhook error", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                """{"status":"error","message":"${e.message}"}""")
        }
    }
}
