package com.yugesa.calmtasks.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yugesa.calmtasks.data.CalmTasksDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = CalmTasksDatabase.get(context).taskDao().getFutureReminders(System.currentTimeMillis())
                tasks.forEach { task ->
                    ReminderScheduler.schedule(context, task.id, task.title, task.reminderAt)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
