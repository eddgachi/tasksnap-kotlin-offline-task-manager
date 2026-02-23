package com.example.tasksnap.domain.repository

import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.model.Project
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level repository interface.
 * This defines WHAT operations are available, not HOW they're implemented.
 * The data layer (TaskRepositoryImpl) will handle Room + Retrofit + sync logic.
 *
 * Why a Flow<List<Task>>?
 * - Reactive: UI subscribes once, gets updates whenever DB changes
 * - Testable: Easy to mock and verify
 * - Coroutine-native: integrates with ViewModel scope
 */
interface TaskRepository {

    // ============ READ OPERATIONS ============

    /**
     * Observe all tasks in a project.
     * Always reads from local DB (offline-first principle).
     * Background worker keeps DB in sync with server.
     */
    fun observeTasksByProject(projectId: String): Flow<List<Task>>

    /**
     * Get all tasks, optionally filtered and sorted.
     */
    fun observeAllTasks(
        filterCompleted: Boolean? = null,  // null = all, true = only completed, false = only pending
        sortBy: TaskSort = TaskSort.CREATED_DESC
    ): Flow<List<Task>>

    /**
     * Single fetch (not a stream) for detail screen.
     * Still reads from local DB first.
     */
    suspend fun getTaskById(id: String): Task?

    // ============ WRITE OPERATIONS ============

    /**
     * Create a new task.
     * - Mark as PENDING_SYNC
     * - Return the created Task (with server-assigned ID once synced)
     * - WorkManager will sync this to server asynchronously
     */
    suspend fun createTask(task: Task): Task

    /**
     * Update an existing task.
     * - Mark as PENDING_SYNC if changes made
     * - WorkManager will sync
     */
    suspend fun updateTask(task: Task)

    /**
     * Soft delete: mark as PENDING_DELETE, let WorkManager handle remote deletion.
     * Why soft delete? In offline-first, deletion is async. We can't guarantee
     * the server will delete it immediately, so we track the intent locally.
     */
    suspend fun deleteTask(id: String)

    /**
     * Hard delete (only after sync succeeds).
     * Called by SyncWorker to remove PENDING_DELETE tasks after server confirms.
     */
    suspend fun hardDeleteTask(id: String)

    // ============ PROJECTS ============

    fun observeAllProjects(): Flow<List<Project>>
    suspend fun createProject(project: Project): Project
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(id: String)

    // ============ SYNC ============

    /**
     * Sync pending changes with server.
     * Called by SyncWorker; not exposed to UI directly.
     * Returns: # of items synced (for logging/debugging)
     */
    suspend fun syncPendingChanges(): Int

    /**
     * Observe the number of tasks that are pending sync or pending deletion.
     * UI shows: "X pending sync"
     */
    fun observePendingSyncCount(): Flow<Int>
}

/**
 * Sorting options for tasks.
 */
enum class TaskSort {
    CREATED_ASC,
    CREATED_DESC,
    DUE_ASC,
    DUE_DESC,
    PRIORITY_HIGH_FIRST,
    PRIORITY_LOW_FIRST,
    ALPHABETICAL,
}