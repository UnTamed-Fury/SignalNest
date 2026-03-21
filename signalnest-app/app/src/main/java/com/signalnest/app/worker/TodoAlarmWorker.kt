package com.signalnest.app.worker

import android.content.Context
import androidx.work.*
import com.signalnest.app.SignalNestApp
import com.signalnest.app.notification.AppNotificationManager
import java.util.concurrent.TimeUnit

class TodoAlarmWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app     = applicationContext as SignalNestApp
        val db      = app.db
        val nm      = AppNotificationManager(applicationContext)
        val overdue = db.todos().getOverdue()

        overdue.forEach { todo ->
            nm.showAlarm(
                id    = (9000 + todo.id.toInt()),
                title = todo.title,
                body  = todo.description.ifBlank { "Task is due!" },
            )
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "todo_alarm_check"

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<TodoAlarmWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
