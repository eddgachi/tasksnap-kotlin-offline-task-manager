package com.example.tasksnap.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasksnap.domain.model.Priority
import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.repository.TaskRepository
import com.example.tasksnap.domain.usecase.CreateTaskUseCase
import com.example.tasksnap.domain.usecase.DeleteTaskUseCase
import com.example.tasksnap.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────────────────────────

data class TaskDetailUiState(
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDateTime? = null,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)

sealed class TaskDetailUiEvent {
    data class TitleChanged(val title: String) : TaskDetailUiEvent()
    data class DescriptionChanged(val desc: String) : TaskDetailUiEvent()
    data class PriorityChanged(val priority: Priority) : TaskDetailUiEvent()
    data class DueDateChanged(val date: LocalDateTime?) : TaskDetailUiEvent()
    object Save : TaskDetailUiEvent()
    object Delete : TaskDetailUiEvent()
    object ClearError : TaskDetailUiEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {

    private val taskId: String? = savedStateHandle.get<String>("taskId")
        ?.takeIf { it != "new" }

    private val _uiState = MutableStateFlow(TaskDetailUiState(taskId = taskId))
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        if (taskId != null) loadTask(taskId)
    }

    fun onEvent(event: TaskDetailUiEvent) {
        when (event) {
            is TaskDetailUiEvent.TitleChanged       -> _uiState.update { it.copy(title = event.title) }
            is TaskDetailUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.desc) }
            is TaskDetailUiEvent.PriorityChanged    -> _uiState.update { it.copy(priority = event.priority) }
            is TaskDetailUiEvent.DueDateChanged     -> _uiState.update { it.copy(dueDate = event.date) }
            is TaskDetailUiEvent.ClearError         -> _uiState.update { it.copy(error = null) }
            is TaskDetailUiEvent.Save               -> save()
            is TaskDetailUiEvent.Delete             -> delete()
        }
    }

    private fun loadTask(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val task: Task? = taskRepository.getTaskById(id)
                if (task != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = task.title,
                            description = task.description,
                            priority = task.priority,
                            dueDate = task.dueDate,
                            isCompleted = task.isCompleted,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Task not found") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load task $id")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                if (taskId == null) {
                    createTaskUseCase(
                        projectId = "default_project",
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate,
                        priority = state.priority,
                    )
                } else {
                    updateTaskUseCase(
                        id = taskId,
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate,
                        clearDueDate = state.dueDate == null,
                        priority = state.priority,
                    )
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save task")
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    private fun delete() {
        val id = taskId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                deleteTaskUseCase(id)
                _uiState.update { it.copy(isDeleting = false, saveSuccess = true) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete task")
                _uiState.update { it.copy(isDeleting = false, error = e.message ?: "Failed to delete") }
            }
        }
    }
}
