package com.example.tasksnap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasksnap.domain.repository.TaskRepository
import com.example.tasksnap.domain.repository.TaskSort
import com.example.tasksnap.domain.usecase.DeleteTaskUseCase
import com.example.tasksnap.domain.usecase.GetTasksUseCase
import com.example.tasksnap.domain.usecase.SyncTasksUseCase
import com.example.tasksnap.domain.usecase.UpdateTaskUseCase
import com.example.tasksnap.presentation.ui.screen.TaskListUiEvent
import com.example.tasksnap.presentation.ui.screen.TaskListUiState
import com.example.tasksnap.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val syncTasksUseCase: SyncTasksUseCase,
    private val taskRepository: TaskRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        observePendingSyncCount()
        observeConnectivity()
    }

    fun onEvent(event: TaskListUiEvent) {
        when (event) {
            is TaskListUiEvent.FilterByCompletion -> filterByCompletion(event.completed)
            is TaskListUiEvent.SortBy -> sortBy(event.sort)
            is TaskListUiEvent.SelectProject -> selectProject(event.projectId)
            is TaskListUiEvent.DeleteTask -> deleteTask(event.taskId)
            is TaskListUiEvent.CompleteTask -> completeTask(event.taskId)
            is TaskListUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is TaskListUiEvent.ManualSync -> manualSync()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTasksUseCase(
                filterCompleted = _uiState.value.filterCompleted,
                sortBy = _uiState.value.sortBy,
            )
                .catch { error ->
                    Timber.e(error, "Failed to load tasks")
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Unknown error") }
                }
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks, isLoading = false, error = null) }
                }
        }
    }

    private fun filterByCompletion(completed: Boolean?) {
        _uiState.update { it.copy(filterCompleted = completed) }
        loadTasks()
    }

    private fun sortBy(sort: TaskSort) {
        _uiState.update { it.copy(sortBy = sort) }
        loadTasks()
    }

    private fun selectProject(projectId: String) {
        _uiState.update { it.copy(selectedProjectId = projectId) }
        loadTasks()
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete task")
                _uiState.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }

    private fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = _uiState.value.tasks.find { it.id == taskId } ?: return@launch
                updateTaskUseCase(id = taskId, isCompleted = !task.isCompleted)
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle task completion")
                _uiState.update { it.copy(error = "Failed to update: ${e.message}") }
            }
        }
    }

    private fun manualSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                syncTasksUseCase()
                _uiState.update { it.copy(isSyncing = false) }
            } catch (e: Exception) {
                Timber.e(e, "Manual sync failed")
                _uiState.update { it.copy(isSyncing = false, error = "Sync failed: ${e.message}") }
            }
        }
    }

    private fun observePendingSyncCount() {
        viewModelScope.launch {
            taskRepository.observePendingSyncCount()
                .catch { error -> Timber.e(error, "Failed to observe pending sync count") }
                .collect { count -> _uiState.update { it.copy(pendingSyncCount = count) } }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observeIsOnline()
                .collect { isOnline -> _uiState.update { it.copy(isOnline = isOnline) } }
        }
    }
}
