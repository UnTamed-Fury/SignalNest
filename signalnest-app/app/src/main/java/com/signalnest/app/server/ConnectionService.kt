package com.signalnest.app.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.signalnest.app.SignalNestApp
import com.signalnest.app.notification.AppNotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ConnectionService : Service() {

    private val scope    = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wsClient: WsClient? = null
    private val notifMgr by lazy { AppNotificationManager(this) }

    companion object {
        private const val N_ID      = 1
        const val ACTION_START      = "sn.START"
        const val ACTION_STOP       = "sn.STOP"
        const val ACTION_RESTART    = "sn.RESTART"

        fun start(ctx: Context) {
            val i = Intent(ctx, ConnectionService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
        fun stop(ctx: Context)    = ctx.startService(Intent(ctx, ConnectionService::class.java).setAction(ACTION_STOP))
        fun restart(ctx: Context) {
            val i = Intent(ctx, ConnectionService::class.java).setAction(ACTION_RESTART)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP    -> { cleanup(); stopSelf(); return START_NOT_STICKY }
            ACTION_RESTART -> { cleanup(); scope.launch { boot() } }
            else           -> scope.launch { boot() }
        }
        return START_STICKY
    }

    private suspend fun boot() {
        val prefs = SignalNestApp.instance.prefs
        val notif = notifMgr.buildServiceNotif("Connecting…")
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(N_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else
            startForeground(N_ID, notif)

        val url   = prefs.serverUrl.first()
        val token = prefs.wsToken.first()
        if (url.isBlank() || token.isBlank()) {
            updateNotif("Not configured — open the app")
            return
        }

        wsClient?.disconnect()
        wsClient = WsClient(scope) { state ->
            val txt = when (state) {
                is WsState.Connected    -> "Connected ✓"
                is WsState.Connecting   -> "Connecting…"
                is WsState.Disconnected -> "Reconnecting…"
                is WsState.Error        -> "Error: ${state.msg}"
            }
            updateNotif(txt)
        }.also { it.connect(url, token) }
    }

    private fun updateNotif(s: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(N_ID, notifMgr.buildServiceNotif(s))
    }

    private fun cleanup() { wsClient?.disconnect(); wsClient = null }

    override fun onDestroy() { cleanup(); scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
