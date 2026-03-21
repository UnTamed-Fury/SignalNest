package com.signalnest.app

import android.app.Application
import com.signalnest.app.data.AppDatabase
import com.signalnest.app.data.PreferencesManager
import com.signalnest.app.notification.AppNotificationManager
import com.signalnest.app.worker.TodoAlarmWorker

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
