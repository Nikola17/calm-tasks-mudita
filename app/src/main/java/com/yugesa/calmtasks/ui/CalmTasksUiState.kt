package com.yugesa.calmtasks.ui

import com.yugesa.calmtasks.data.FolderEntity
import com.yugesa.calmtasks.data.TaskEntity
import java.time.LocalDate

data class CalmTasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val todayPriorityLimit: Int = 3,
    val selectedDate: LocalDate = LocalDate.now(),
    val screen: Screen = Screen.Today,
)
