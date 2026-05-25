package com.yugesa.calmtasks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CalmTasksRepository(
    private val taskDao: TaskDao,
    private val folderDao: FolderDao,
    private val settingsDao: SettingsDao,
) {
    val tasks: Flow<List<TaskEntity>> = taskDao.observeTasks()
    val folders: Flow<List<FolderEntity>> = folderDao.observeFolders()
    val settings: Flow<SettingsEntity> = settingsDao.observeSettings().map { it ?: SettingsEntity() }

    suspend fun ensureDefaults() {
        if (folderDao.countFolders() == 0) {
            folderDao.insertFolders(DefaultFolders.all)
        }
        if (settingsDao.getSettings() == null) {
            settingsDao.saveSettings(SettingsEntity())
        }
    }

    suspend fun addTask(
        title: String,
        folderId: Long?,
        plannedDate: String?,
        reminderAt: Long?,
    ): Long {
        val now = System.currentTimeMillis()
        return taskDao.insertTask(
            TaskEntity(
                title = title.trim(),
                folderId = folderId,
                plannedDate = plannedDate,
                reminderAt = reminderAt,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(title = task.title.trim(), updatedAt = System.currentTimeMillis()))
    }

    suspend fun markDone(taskId: Long) {
        taskDao.setStatus(taskId, TaskEntity.STATUS_DONE, System.currentTimeMillis())
    }

    suspend fun restoreTask(taskId: Long) {
        taskDao.restoreStatus(taskId, TaskEntity.STATUS_ACTIVE, System.currentTimeMillis())
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    suspend fun moveLater(taskId: Long) {
        taskDao.moveLater(taskId, System.currentTimeMillis())
    }

    suspend fun updatePriorityLimit(limit: Int) {
        settingsDao.saveSettings(SettingsEntity(todayPriorityLimit = limit.coerceIn(1, 99)))
    }

    suspend fun addFolder(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        if (folderDao.getFolders().any { it.customName.equals(cleanName, ignoreCase = true) }) return
        folderDao.insertFolder(
            FolderEntity(
                nameKey = "folder_custom",
                sortOrder = folderDao.maxSortOrder() + 1,
                customName = cleanName,
            ),
        )
    }

    suspend fun renameFolder(folderId: Long, name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val folders = folderDao.getFolders()
        if (folders.any { it.id != folderId && it.customName.equals(cleanName, ignoreCase = true) }) return
        folderDao.renameCustomFolder(folderId, cleanName)
    }

    suspend fun deleteCustomFolder(folderId: Long) {
        taskDao.moveTasksToUnplanned(folderId, System.currentTimeMillis())
        folderDao.deleteCustomFolder(folderId)
    }

    suspend fun moveFolder(folderId: Long, direction: Int) {
        val folders = folderDao.getFolders()
        val index = folders.indexOfFirst { it.id == folderId }
        val targetIndex = index + direction
        if (index !in folders.indices || targetIndex !in folders.indices) return
        val current = folders[index]
        val target = folders[targetIndex]
        folderDao.updateSortOrder(current.id, target.sortOrder)
        folderDao.updateSortOrder(target.id, current.sortOrder)
    }
}
