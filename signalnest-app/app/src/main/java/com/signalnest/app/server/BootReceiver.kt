package fury.signalnest.app.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fury.signalnest.app.SignalNestApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = (ctx.applicationContext as SignalNestApp).prefs
                if (prefs.onboarded.first()) ConnectionService.start(ctx)
            }
        }
    }
}
