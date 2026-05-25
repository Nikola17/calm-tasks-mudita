package com.yugesa.calmtasks.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, FolderEntity::class, SettingsEntity::class, ReminderHistoryEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class CalmTasksDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun folderDao(): FolderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun reminderHistoryDao(): ReminderHistoryDao

    companion object {
        @Volatile private var instance: CalmTasksDatabase? = null

        fun get(context: Context): CalmTasksDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CalmTasksDatabase::class.java,
                    "calm_tasks.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE folders ADD COLUMN customName TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS reminder_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "taskId INTEGER NOT NULL, " +
                        "taskTitle TEXT NOT NULL, " +
                        "remindedAt INTEGER NOT NULL)",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminder_history ADD COLUMN eventType TEXT NOT NULL DEFAULT 'triggered'")
            }
        }
    }
}
