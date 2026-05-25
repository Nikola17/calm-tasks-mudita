package com.yugesa.calmtasks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yugesa.calmtasks.data.CalmTasksDatabase
import com.yugesa.calmtasks.data.CalmTasksRepository
import com.yugesa.calmtasks.data.FolderEntity
import com.yugesa.calmtasks.data.SettingsEntity
import com.yugesa.calmtasks.data.TaskEntity
import com.yugesa.calmtasks.reminders.ReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalmTasksViewModel(
    private val app: Application,
) : AndroidViewModel(app) {
    private val database = CalmTasksDatabase.get(app)
    private val repository = CalmTasksRepository(
        database.taskDao(),
        database.folderDao(),
        database.settingsDao(),
    )
    private val screen = MutableStateFlow<Screen>(Screen.Today)
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val dataState = combine(
        repository.tasks,
        repository.folders,
        repository.settings,
    ) { tasks, folders, settings ->
        CalmTasksDataState(tasks, folders, settings)
    }

    val uiState = combine(dataState, screen, selectedDate) { data, currentScreen, currentDate ->
        CalmTasksUiState(
            tasks = data.tasks,
            folders = data.folders,
            todayPriorityLimit = data.settings.todayPriorityLimit,
            selectedDate = currentDate,
            screen = currentScreen,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalmTasksUiState())

    init {
        viewModelScope.launch {
            repository.ensureDefaults()
        }
    }

    fun goTo(next: Screen) {
        screen.value = next
    }

    fun back() {
        screen.update { Screen.Today }
    }

    fun previousDay() {
        selectedDate.update { current ->
            if (current.isAfter(LocalDate.now())) current.minusDays(1) else current
        }
    }

    fun nextDay() {
        selectedDate.update { it.plusDays(1) }
    }

    fun addTask(title: String, folderId: Long?, plannedForSelectedDay: Boolean, reminderAt: Long?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val taskId = repository.addTask(
                title = title,
                folderId = folderId,
                plannedDate = if (plannedForSelectedDay) selectedDate.value.toString() else null,
                reminderAt = reminderAt,
            )
            ReminderScheduler.schedule(app, taskId, title.trim(), reminderAt)
            screen.value = Screen.Today
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            ReminderScheduler.schedule(app, task.id, task.title, task.reminderAt)
            screen.value = Screen.Today
        }
    }

    fun markDone(taskId: Long) {
        viewModelScope.launch {
            repository.markDone(taskId)
            ReminderScheduler.cancel(app, taskId)
        }
    }

    fun restoreTask(taskId: Long) {
        viewModelScope.launch {
            repository.restoreTask(taskId)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            ReminderScheduler.cancel(app, taskId)
            screen.value = Screen.Today
        }
    }

    fun deleteDoneTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun moveLater(taskId: Long) {
        viewModelScope.launch {
            repository.moveLater(taskId)
        }
    }

    fun updatePriorityLimit(limit: Int) {
        viewModelScope.launch {
            repository.updatePriorityLimit(limit)
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            repository.addFolder(name)
        }
    }

    fun renameFolder(folderId: Long, name: String) {
        viewModelScope.launch {
            repository.renameFolder(folderId, name)
        }
    }

    fun deleteCustomFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteCustomFolder(folderId)
        }
    }

    fun moveFolderUp(folderId: Long) {
        viewModelScope.launch {
            repository.moveFolder(folderId, -1)
        }
    }

    fun moveFolderDown(folderId: Long) {
        viewModelScope.launch {
            repository.moveFolder(folderId, 1)
        }
    }
}

private data class CalmTasksDataState(
    val tasks: List<TaskEntity>,
    val folders: List<FolderEntity>,
    val settings: SettingsEntity,
)
