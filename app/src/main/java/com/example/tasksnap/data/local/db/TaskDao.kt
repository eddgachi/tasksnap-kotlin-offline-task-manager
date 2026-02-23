package com.example.tasksnap.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tasksnap.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task entity.
 * Room generates the SQL at compile-time (type-safe queries).
 *
 * All queries return Flow to support reactive updates.
 * When a task is updated, all Flow collectors are notified automatically.
 */
@Dao
interface TaskDao {

    // ============ READ ============

    /**
     * Observe all tasks in a project.
     * Returns Flow so UI gets notified of changes in real-time.
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE projectId = :projectId
        ORDER BY isCompleted ASC, updatedAt DESC
        """
    )
    fun observeTasksByProject(projectId: String): Flow<List<TaskEntity>>

    /**
     * Observe all tasks with optional filtering.
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE (:filterCompleted IS NULL OR isCompleted = :filterCompleted)
        ORDER BY updatedAt DESC
        """
    )
    fun observeAllTasks(filterCompleted: Boolean?): Flow<List<TaskEntity>>

    /**
     * Single fetch for detail view.
     * Not a Flow—we only need it once when opening task detail.
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    /**
     * Get all tasks with a specific sync status.
     * Used by SyncWorker to find what needs syncing.
     */
    @Query("SELECT * FROM tasks WHERE syncStatus = :syncStatus")
    suspend fun getTasksBySyncStatus(syncStatus: String): List<TaskEntity>

    /**
     * Count pending changes (for debugging/UI indicator).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_DELETE')")
    fun observePendingSyncCount(): Flow<Int>

    // ============ WRITE ============

    /**
     * Insert a new task.
     * If task.id already exists (which it will—we generate locally), this throws.
     * Use insertOrIgnore or insertOrReplace if needed.
     */
    @Insert
    suspend fun insertTask(task: TaskEntity)

    /**
     * Update an existing task (must exist, or throws).
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Upsert (insert or replace).
     * Used during sync when server might return updated data.
     */
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    /**
     * Delete a task permanently.
     * Only called after server confirms deletion.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Hard delete by ID.
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    /**
     * Update only the sync status (used after successful sync).
     */
    @Query("UPDATE tasks SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, updatedAt: java.time.LocalDateTime)

    /**
     * Batch update sync status (for syncing multiple tasks).
     */
    @Query("UPDATE tasks SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun updateSyncStatusBatch(ids: List<String>, syncStatus: String, updatedAt: java.time.LocalDateTime)
}