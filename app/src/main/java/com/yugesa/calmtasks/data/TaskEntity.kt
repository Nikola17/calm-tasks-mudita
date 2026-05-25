package com.yugesa.calmtasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val folderId: Long?,
    val status: String = STATUS_ACTIVE,
    val plannedDate: String?,
    val reminderAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_DONE = "done"
    }
}

