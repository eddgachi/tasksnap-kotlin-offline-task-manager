package com.example.tasksnap.data.remote.datasource

import com.example.tasksnap.domain.model.Project
import com.example.tasksnap.domain.model.SyncStatus
import com.example.tasksnap.domain.model.Task
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock remote data source.
 * Simulates a REST API without actual HTTP calls.
 *
 * Later, we'll replace this with actual Retrofit calls to a real backend.
 * The repository won't know the difference—it only depends on this interface.
 */
class MockRemoteTaskDataSource @Inject constructor() {

    // Simulate network delay (so UI threading behavior is realistic)
    private suspend fun simulateNetworkDelay() {
        delay(500) // Half second delay like a real API
    }

    /**
     * Simulate uploading a task to the server.
     * Returns: Task with server-assigned ID (if applicable) and SYNCED status.
     *
     * In a real API:
     * POST /api/tasks
     * { title: "...", description: "...", ... }
     * Returns: { id: "server-uuid", ... }
     */
    suspend fun uploadTask(task: Task): Task {
        simulateNetworkDelay()

        // Simulate server accepting the task and confirming it
        return task.copy(syncStatus = SyncStatus.SYNCED)
    }

    /**
     * Simulate uploading multiple tasks (batch sync).
     */
    suspend fun uploadTasks(tasks: List<Task>): List<Task> {
        simulateNetworkDelay()
        return tasks.map { it.copy(syncStatus = SyncStatus.SYNCED) }
    }

    /**
     * Simulate updating a task on the server.
     * PUT /api/tasks/{id}
     */
    suspend fun updateTaskRemote(task: Task): Task {
        simulateNetworkDelay()
        return task.copy(syncStatus = SyncStatus.SYNCED)
    }

    /**
     * Simulate deleting a task on the server.
     * DELETE /api/tasks/{id}
     */
    suspend fun deleteTaskRemote(taskId: String) {
        simulateNetworkDelay()
        // Server confirms deletion
    }

    // ============ PROJECTS ============

    suspend fun uploadProject(project: Project): Project {
        simulateNetworkDelay()
        return project.copy(syncStatus = SyncStatus.SYNCED)
    }

    suspend fun updateProjectRemote(project: Project): Project {
        simulateNetworkDelay()
        return project.copy(syncStatus = SyncStatus.SYNCED)
    }

    suspend fun deleteProjectRemote(projectId: String) {
        simulateNetworkDelay()
    }
}