package com.yugesa.calmtasks.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yugesa.calmtasks.MainActivity
import com.yugesa.calmtasks.R
import com.yugesa.calmtasks.data.CalmTasksDatabase
import com.yugesa.calmtasks.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, -1L)
        if (taskId <= 0) return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_TITLE).orEmpty()

        when (intent.action) {
            ReminderScheduler.ACTION_DONE -> updateTask(context, taskId, done = true)
            ReminderScheduler.ACTION_LATER -> updateTask(context, taskId, done = false)
            else -> showNotification(context, taskId, title)
        }
    }

    private fun updateTask(context: Context, taskId: Long, done: Boolean) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val dao = CalmTasksDatabase.get(context).taskDao()
            val now = System.currentTimeMillis()
            val task = dao.getTask(taskId)
            if (done) {
                dao.setStatus(taskId, TaskEntity.STATUS_DONE, now)
                ReminderScheduler.cancel(context, taskId)
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
            } else if (task != null) {
                val later = now + 60 * 60 * 1000
                dao.setReminder(taskId, later, now)
                ReminderScheduler.schedule(context, taskId, task.title, later)
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
            }
            pending.finish()
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ReminderNotifications.ensureChannel(context)

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderNotifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(title)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_notification, context.getString(R.string.done), ReminderScheduler.pendingIntent(context, taskId, title, ReminderScheduler.ACTION_DONE))
            .addAction(R.drawable.ic_notification, context.getString(R.string.later), ReminderScheduler.pendingIntent(context, taskId, title, ReminderScheduler.ACTION_LATER))
            .addAction(R.drawable.ic_notification, context.getString(R.string.open), openPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
    }
}
