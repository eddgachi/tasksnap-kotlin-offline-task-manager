package com.example.tasksnap.domain.usecase

import com.example.tasksnap.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case: Sync all pending changes with the server.
 *
 * Can throw:
 * - NetworkException: Network unavailable, timeout, server 500, etc.
 * - AuthException: 401, 403 (user not authenticated)
 * - Exception: Unknown errors
 */
class SyncTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {

    /**
     * Execute sync.
     * @return Number of tasks successfully synced
     * @throws NetworkException if network fails
     * @throws AuthException if authentication fails
     */
    suspend operator fun invoke(): Int {
        return taskRepository.syncPendingChanges()
    }
}