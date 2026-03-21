package com.signalnest.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    fun relative(ms: Long): String {
        val d = System.currentTimeMillis() - ms
        return when {
            d < 60_000      -> "just now"
            d < 3_600_000   -> "${d / 60_000}m ago"
            d < 86_400_000  -> "${d / 3_600_000}h ago"
            d < 604_800_000 -> "${d / 86_400_000}d ago"
            else            -> fmt.format(Date(ms))
        }
    }

    fun format(ms: Long): String = fmt.format(Date(ms))
}

object NetworkUtils {
    fun localIp(ctx: Context): String? = try {
        val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wm.connectionInfo.ipAddress
        if (ip == 0) fallbackIp()
        else "%d.%d.%d.%d".format(ip and 0xff, ip shr 8 and 0xff, ip shr 16 and 0xff, ip shr 24 and 0xff)
    } catch (_: Exception) { fallbackIp() }

    private fun fallbackIp(): String? = try {
        java.net.NetworkInterface.getNetworkInterfaces()?.toList()
            ?.flatMap { it.inetAddresses.toList() }
            ?.firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains('.') == true }
            ?.hostAddress
    } catch (_: Exception) { null }
}
