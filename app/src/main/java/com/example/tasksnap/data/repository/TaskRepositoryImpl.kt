package com.example.tasksnap.data.repository

import com.example.tasksnap.data.local.datasource.LocalTaskDataSource
import com.example.tasksnap.data.remote.datasource.MockRemoteTaskDataSource
import com.example.tasksnap.domain.model.Project
import com.example.tasksnap.domain.model.SyncStatus
import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.repository.TaskRepository
import com.example.tasksnap.domain.repository.TaskSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Implementation of TaskRepository.
 * The UI always reads from local DB (offline-first).
 * WorkManager keeps the local DB in sync with the server.
 */
class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: LocalTaskDataSource,
    private val remoteDataSource: MockRemoteTaskDataSource,
) : TaskRepository {

    override fun observeTasksByProject(projectId: String): Flow<List<Task>> =
        localDataSource.observeTasksByProject(projectId)

    override fun observeAllTasks(
        filterCompleted: Boolean?,
        sortBy: TaskSort,
    ): Flow<List<Task>> =
        localDataSource.observeAllTasks(filterCompleted)
            .map { tasks -> tasks.sortedBy(sortBy) }

    override suspend fun getTaskById(id: String): Task? =
        localDataSource.getTaskById(id)

    override suspend fun createTask(task: Task): Task {
        require(task.syncStatus == SyncStatus.PENDING_SYNC) {
            "New tasks must have PENDING_SYNC status"
        }
        localDataSource.insertTask(task)
        return task
    }

    override suspend fun updateTask(task: Task) {
        require(task.syncStatus == SyncStatus.PENDING_SYNC) {
            "Updated tasks must have PENDING_SYNC status"
        }
        localDataSource.updateTask(task)
    }

    override suspend fun deleteTask(id: String) {
        val task = localDataSource.getTaskById(id) ?: return
        val updated = task.copy(
            syncStatus = SyncStatus.PENDING_DELETE,
            updatedAt = LocalDateTime.now(),
        )
        localDataSource.updateTask(updated)
    }

    override suspend fun hardDeleteTask(id: String) {
        localDataSource.deleteTaskById(id)
    }

    override fun observeAllProjects(): Flow<List<Project>> =
        localDataSource.observeAllProjects()

    override suspend fun createProject(project: Project): Project {
        localDataSource.insertProject(project)
        return project
    }

    override suspend fun updateProject(project: Project) {
        localDataSource.updateProject(project)
    }

    override suspend fun deleteProject(id: String) {
        localDataSource.deleteProjectById(id)
    }

    override fun observePendingSyncCount(): Flow<Int> =
        localDataSource.observePendingSyncCount()

    override suspend fun syncPendingChanges(): Int {
        var syncedCount = 0
        try {
            val pendingSyncTasks = localDataSource.getTasksBySyncStatus(SyncStatus.PENDING_SYNC.name)
            if (pendingSyncTasks.isNotEmpty()) {
                val synced = remoteDataSource.uploadTasks(pendingSyncTasks)
                synced.forEach { localDataSource.updateTaskSyncStatus(it.id, SyncStatus.SYNCED.name) }
                syncedCount += synced.size
            }

            val pendingDeleteTasks = localDataSource.getTasksBySyncStatus(SyncStatus.PENDING_DELETE.name)
            pendingDeleteTasks.forEach { toDelete ->
                try {
                    remoteDataSource.deleteTaskRemote(toDelete.id)
                    hardDeleteTask(toDelete.id)
                    syncedCount++
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete task ${toDelete.id}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync failed after $syncedCount items")
            if (syncedCount == 0) throw e
        }
        return syncedCount
    }

    private fun List<Task>.sortedBy(sortBy: TaskSort): List<Task> = when (sortBy) {
        TaskSort.CREATED_ASC -> sortedBy { it.createdAt }
        TaskSort.CREATED_DESC -> sortedByDescending { it.createdAt }
        TaskSort.DUE_ASC -> sortedWith(compareBy { it.dueDate ?: LocalDateTime.MAX })
        TaskSort.DUE_DESC -> sortedWith(compareByDescending { it.dueDate ?: LocalDateTime.MIN })
        TaskSort.PRIORITY_HIGH_FIRST -> sortedWith(compareBy { it.priority.ordinal })
        TaskSort.PRIORITY_LOW_FIRST -> sortedWith(compareByDescending { it.priority.ordinal })
        TaskSort.ALPHABETICAL -> sortedBy { it.title }
    }
}