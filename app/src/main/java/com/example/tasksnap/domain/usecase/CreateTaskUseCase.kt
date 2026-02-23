package com.example.tasksnap.domain.usecase

import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.model.SyncStatus
import com.example.tasksnap.domain.repository.TaskRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use case: Create a new task.
 *
 * Responsibilities:
 * 1. Validate input (title is not empty)
 * 2. Generate an ID (offline-first: generate locally, might change on server sync)
 * 3. Set default timestamps
 * 4. Mark as PENDING_SYNC
 * 5. Delegate to repository
 *
 * Why a separate use case vs. calling repo directly in ViewModel?
 * - Testability: Can test validation and ID generation independently
 * - Reusability: Multiple screens might create tasks
 * - Business rules: If logic changes (e.g., validate title length), it's in one place
 */
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {

    /**
     * Execute the use case.
     *
     * @param projectId Which project this task belongs to
     * @param title Task title (validated: must not be empty)
     * @param description Optional details
     * @param dueDate Optional due date
     * @param priority Task priority
     * @return The created task (with local ID)
     * @throws IllegalArgumentException if title is empty
     */
    suspend operator fun invoke(
        projectId: String,
        title: String,
        description: String = "",
        dueDate: LocalDateTime? = null,
        priority: com.example.tasksnap.domain.model.Priority = com.example.tasksnap.domain.model.Priority.MEDIUM,
    ): Task {
        // Validate: title must not be blank
        require(title.isNotBlank()) { "Task title cannot be empty" }

        // Generate a unique local ID (will be replaced on server sync if needed)
        val localId = UUID.randomUUID().toString()

        val now = LocalDateTime.now()

        val newTask = Task(
            id = localId,
            projectId = projectId,
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            priority = priority,
            isCompleted = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC,  // Will be synced by WorkManager
        )

        return taskRepository.createTask(newTask)
    }
}