package fury.signalnest.app

import android.app.Application
import fury.signalnest.app.data.AppDatabase
import fury.signalnest.app.data.PreferencesManager
import fury.signalnest.app.notification.AppNotificationManager
import fury.signalnest.app.worker.TodoAlarmWorker

class SignalNestApp : Application() {
    val db    by lazy { AppDatabase.getInstance(this) }
    val prefs by lazy { PreferencesManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppNotificationManager(this).createChannels()
        TodoAlarmWorker.schedule(this)
    }

    companion object {
        lateinit var instance: SignalNestApp private set
    }
}
