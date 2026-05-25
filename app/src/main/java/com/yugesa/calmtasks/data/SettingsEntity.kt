package com.yugesa.calmtasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val todayPriorityLimit: Int = 3,
)

