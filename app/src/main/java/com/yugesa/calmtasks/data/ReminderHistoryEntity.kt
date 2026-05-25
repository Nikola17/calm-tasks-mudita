package com.yugesa.calmtasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_history")
data class ReminderHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val remindedAt: Long,
    val eventType: String = "triggered",
) {
    companion object {
        const val EVENT_TRIGGERED = "triggered"
        const val EVENT_DONE = "done"
        const val EVENT_SNOOZED = "snoozed"
    }
}
