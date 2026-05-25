package com.yugesa.calmtasks.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY sortOrder ASC")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY sortOrder ASC")
    suspend fun getFolders(): List<FolderEntity>

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun countFolders(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Insert
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM folders")
    suspend fun maxSortOrder(): Int

    @Query("UPDATE folders SET customName = :name WHERE id = :id AND customName IS NOT NULL")
    suspend fun renameCustomFolder(id: Long, name: String)

    @Query("UPDATE folders SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("DELETE FROM folders WHERE id = :id AND customName IS NOT NULL")
    suspend fun deleteCustomFolder(id: Long)
}
