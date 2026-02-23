package com.example.tasksnap.domain.usecase

import com.example.tasksnap.domain.model.Priority
import com.example.tasksnap.domain.model.SyncStatus
import com.example.tasksnap.domain.repository.TaskRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    /**
     * @param clearDueDate pass true to explicitly set dueDate to null.
     *   Using null for [dueDate] alone means "don't change the existing date".
     */
    suspend operator fun invoke(
        id: String,
        title: String? = null,
        description: String? = null,
        dueDate: LocalDateTime? = null,
        clearDueDate: Boolean = false,
        priority: Priority? = null,
        isCompleted: Boolean? = null,
    ) {
        val current = taskRepository.getTaskById(id)
            ?: throw IllegalArgumentException("Task not found: $id")

        val newTitle = title?.takeIf { it.isNotBlank() }?.trim() ?: current.title
        require(newTitle.isNotBlank()) { "Title cannot be empty" }

        val newDueDate = when {
            clearDueDate -> null
            dueDate != null -> dueDate
            else -> current.dueDate
        }

        val changed = newTitle != current.title ||
            (description?.trim() ?: current.description) != current.description ||
            newDueDate != current.dueDate ||
            (priority ?: current.priority) != current.priority ||
            (isCompleted ?: current.isCompleted) != current.isCompleted

        val updatedTask = current.copy(
            title = newTitle,
            description = description?.trim() ?: current.description,
            dueDate = newDueDate,
            priority = priority ?: current.priority,
            isCompleted = isCompleted ?: current.isCompleted,
            updatedAt = LocalDateTime.now(),
            syncStatus = if (changed) SyncStatus.PENDING_SYNC else current.syncStatus,
        )

        taskRepository.updateTask(updatedTask)
    }
}
