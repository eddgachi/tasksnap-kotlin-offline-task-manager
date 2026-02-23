package com.example.tasksnap.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tasksnap.data.local.db.ProjectDao
import com.example.tasksnap.data.local.db.TaskDao
import com.example.tasksnap.data.local.db.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(
        @ApplicationContext context: Context
    ): TaskDatabase = Room.databaseBuilder(
        context,
        TaskDatabase::class.java,
        "task_manager.db"
    )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed a default project so tasks can be created on first launch.
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                db.execSQL(
                    "INSERT INTO projects (id, name, description, color, createdAt, updatedAt, syncStatus) " +
                        "VALUES ('default_project', 'My Tasks', NULL, '#4F46E5', '$now', '$now', 'SYNCED')"
                )
            }
        })
        .build()

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideProjectDao(database: TaskDatabase): ProjectDao = database.projectDao()
}
