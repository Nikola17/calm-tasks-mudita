package com.yugesa.calmtasks.data

object DefaultFolders {
    const val HOME_ID = 1L
    const val WORK_ID = 2L
    const val ADMIN_ID = 3L
    const val ERRANDS_ID = 4L
    const val PERSONAL_ID = 5L

    val all = listOf(
        FolderEntity(HOME_ID, "folder_home", 0),
        FolderEntity(WORK_ID, "folder_work", 1),
        FolderEntity(ADMIN_ID, "folder_admin", 2),
        FolderEntity(ERRANDS_ID, "folder_errands", 3),
        FolderEntity(PERSONAL_ID, "folder_personal", 4),
    )
}
