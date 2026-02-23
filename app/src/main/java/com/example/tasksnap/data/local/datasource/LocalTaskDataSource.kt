package com.example.tasksnap.data.local.datasource

import com.example.tasksnap.data.local.db.ProjectDao
import com.example.tasksnap.data.local.db.TaskDao
import com.example.tasksnap.data.local.entity.toDomain
import com.example.tasksnap.data.local.entity.toEntity
import com.example.tasksnap.domain.model.Project
import com.example.tasksnap.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Local data source: converts domain models ↔ Room entities and
 * delegates to the DAOs. minSdk = 26, so java.time is always available.
 */
class LocalTaskDataSource @Inject constructor(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
) {

    // ── Tasks ──────────────────────────────────────────────────────────────

    fun observeTasksByProject(projectId: String): Flow<List<Task>> =
        taskDao.observeTasksByProject(projectId).map { it.map { e -> e.toDomain() } }

    fun observeAllTasks(filterCompleted: Boolean?): Flow<List<Task>> =
        taskDao.observeAllTasks(filterCompleted).map { it.map { e -> e.toDomain() } }

    suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)?.toDomain()

    suspend fun getTasksBySyncStatus(syncStatus: String): List<Task> =
        taskDao.getTasksBySyncStatus(syncStatus).map { it.toDomain() }

    fun observePendingSyncCount(): Flow<Int> = taskDao.observePendingSyncCount()

    suspend fun insertTask(task: Task) = taskDao.insertTask(task.toEntity())

    suspend fun updateTask(task: Task) = taskDao.updateTask(task.toEntity())

    suspend fun upsertTask(task: Task) = taskDao.upsertTask(task.toEntity())

    suspend fun deleteTaskById(id: String) = taskDao.deleteTaskById(id)

    suspend fun updateTaskSyncStatus(id: String, syncStatus: String) =
        taskDao.updateSyncStatus(id, syncStatus, LocalDateTime.now())

    suspend fun updateTaskSyncStatusBatch(ids: List<String>, syncStatus: String) =
        taskDao.updateSyncStatusBatch(ids, syncStatus, LocalDateTime.now())

    // ── Projects ───────────────────────────────────────────────────────────

    fun observeAllProjects(): Flow<List<Project>> =
        projectDao.observeAllProjects().map { it.map { e -> e.toDomain() } }

    suspend fun getProjectById(id: String): Project? = projectDao.getProjectById(id)?.toDomain()

    suspend fun insertProject(project: Project) = projectDao.insertProject(project.toEntity())

    suspend fun updateProject(project: Project) = projectDao.updateProject(project.toEntity())

    suspend fun deleteProjectById(id: String) = projectDao.deleteProjectById(id)
}
