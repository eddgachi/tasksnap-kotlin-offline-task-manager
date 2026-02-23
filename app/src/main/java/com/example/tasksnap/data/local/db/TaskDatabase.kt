package com.example.tasksnap.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tasksnap.data.local.entity.ProjectEntity
import com.example.tasksnap.data.local.entity.TaskEntity
import com.example.tasksnap.util.LocalDateTimeConverter

/**
 * Room database for the Task Manager app.
 *
 * @Database marks this as a Room DB with the specified entities.
 * version = 1: First schema. Increment when making breaking changes.
 *
 * exportSchema = true: Room generates migration schemas (for Play Console crash reporting).
 */
@Database(
    entities = [TaskEntity::class, ProjectEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateTimeConverter::class)  // Custom converter for LocalDateTime
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao

    companion object {
        private const val DB_NAME = "task_manager.db"

        /**
         * Singleton pattern: only one DB instance per app process.
         * @Volatile ensures changes are visible across threads.
         */
        @Volatile
        private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(
                context,
                TaskDatabase::class.java,
                DB_NAME
            )
                // We'll add migrations here as the schema evolves
                // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)
                .build()
        }
    }
}