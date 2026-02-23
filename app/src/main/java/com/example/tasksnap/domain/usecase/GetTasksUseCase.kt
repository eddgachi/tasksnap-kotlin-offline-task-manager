package com.example.tasksnap.domain.usecase

import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.repository.TaskRepository
import com.example.tasksnap.domain.repository.TaskSort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Get all tasks with optional filtering and sorting.
 *
 * Returns a Flow so the UI can observe and react to changes.
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {

    operator fun invoke(
        filterCompleted: Boolean? = null,
        sortBy: TaskSort = TaskSort.CREATED_DESC,
    ): Flow<List<Task>> =
        taskRepository.observeAllTasks(filterCompleted, sortBy)
}