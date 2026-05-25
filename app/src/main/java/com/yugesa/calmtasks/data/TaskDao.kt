package com.yugesa.calmtasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: Long): TaskEntity?

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, reminderAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, updatedAt: Long)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreStatus(id: Long, status: String, updatedAt: Long)

    @Query("UPDATE tasks SET plannedDate = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun moveLater(id: Long, updatedAt: Long)

    @Query("UPDATE tasks SET reminderAt = :reminderAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setReminder(id: Long, reminderAt: Long?, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("UPDATE tasks SET folderId = NULL, updatedAt = :updatedAt WHERE folderId = :folderId")
    suspend fun moveTasksToUnplanned(folderId: Long, updatedAt: Long)
}
