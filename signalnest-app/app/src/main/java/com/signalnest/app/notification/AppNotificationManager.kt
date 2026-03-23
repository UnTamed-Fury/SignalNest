package fury.signalnest.app.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import fury.signalnest.app.MainActivity

object Ch {
    const val EVENTS  = "sn_events"
    const val SILENT  = "sn_silent"
    const val SERVICE = "sn_service"
    const val ALARMS  = "sn_alarms"
}

class AppNotificationManager(private val ctx: Context) {

    private val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannels() {
        listOf(
            NotificationChannel(Ch.EVENTS,  "Events",          NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true); setShowBadge(true) },
            NotificationChannel(Ch.SILENT,  "Silent events",   NotificationManager.IMPORTANCE_MIN).apply  { setShowBadge(false) },
            NotificationChannel(Ch.SERVICE, "Connection",      NotificationManager.IMPORTANCE_LOW).apply  { setShowBadge(false) },
            NotificationChannel(Ch.ALARMS,  "Todo reminders",  NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true) },
        ).forEach { mgr.createNotificationChannel(it) }
    }

    private fun mainIntent(id: Int): PendingIntent = PendingIntent.getActivity(
        ctx, id,
        Intent(ctx, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    fun showEvent(id: Int, title: String, body: String, silent: Boolean, sound: Boolean, vib: Boolean) {
        val channel = if (silent) Ch.SILENT else Ch.EVENTS
        val n = NotificationCompat.Builder(ctx, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (silent) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainIntent(id))
            .setAutoCancel(true)
            .apply {
                if (!silent && sound) setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                if (!silent && vib)   setVibrate(longArrayOf(0, 200, 100, 200))
            }.build()
        mgr.notify(id, n)
    }

    fun showAlarm(id: Int, title: String, body: String) {
        val n = NotificationCompat.Builder(ctx, Ch.ALARMS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $title")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainIntent(id))
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()
        mgr.notify(id, n)
    }

    fun buildServiceNotif(status: String): Notification =
        NotificationCompat.Builder(ctx, Ch.SERVICE)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("SignalNest")
            .setContentText(status)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
}
