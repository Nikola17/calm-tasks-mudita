package com.yugesa.calmtasks.ui

sealed interface Screen {
    data object Today : Screen
    data object AddTask : Screen
    data object Inbox : Screen
    data object Folders : Screen
    data object DoneTasks : Screen
    data object Settings : Screen
    data object FocusReview : Screen
    data class FolderDetail(val folderId: Long) : Screen
    data class TaskDetail(val taskId: Long) : Screen
}
