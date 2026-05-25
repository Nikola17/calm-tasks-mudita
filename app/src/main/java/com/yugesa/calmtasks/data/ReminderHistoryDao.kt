package com.yugesa.calmtasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderHistoryDao {
    @Query("SELECT * FROM reminder_history ORDER BY remindedAt DESC")
    fun observeHistory(): Flow<List<ReminderHistoryEntity>>

    @Insert
    suspend fun insert(history: ReminderHistoryEntity): Long
}

