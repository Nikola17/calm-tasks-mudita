package com.yugesa.calmtasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameKey: String,
    val sortOrder: Int,
    val customName: String? = null,
)
