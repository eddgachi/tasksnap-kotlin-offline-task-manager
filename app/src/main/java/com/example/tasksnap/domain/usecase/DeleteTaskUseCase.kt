package com.example.tasksnap.domain.usecase

import com.example.tasksnap.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(taskId: String) {
        taskRepository.deleteTask(taskId)
    }
}