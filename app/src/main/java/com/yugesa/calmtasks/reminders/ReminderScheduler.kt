package com.yugesa.calmtasks.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object ReminderScheduler {
    const val ACTION_SHOW = "com.yugesa.calmtasks.reminders.SHOW"
    const val ACTION_DONE = "com.yugesa.calmtasks.reminders.DONE"
    const val ACTION_LATER = "com.yugesa.calmtasks.reminders.LATER"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"

    fun schedule(context: Context, taskId: Long, title: String, reminderAt: Long?) {
        cancel(context, taskId)
        if (reminderAt == null || reminderAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, taskId, title, ACTION_SHOW)
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
    }

    fun cancel(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, taskId, "", ACTION_SHOW))
    }

    fun pendingIntent(context: Context, taskId: Long, title: String, action: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_TASK_ID, taskId)
            .putExtra(EXTRA_TASK_TITLE, title)
        return PendingIntent.getBroadcast(
            context,
            requestCode(taskId, action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun requestCode(taskId: Long, action: String): Int {
        return (taskId.toInt() * 10) + when (action) {
            ACTION_DONE -> 1
            ACTION_LATER -> 2
            else -> 0
        }
    }
}

