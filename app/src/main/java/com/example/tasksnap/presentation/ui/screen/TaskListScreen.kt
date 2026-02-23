package com.example.tasksnap.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.domain.repository.TaskSort
import com.example.tasksnap.presentation.ui.component.EmptyTasksView
import com.example.tasksnap.presentation.ui.component.TaskCard
import com.example.tasksnap.presentation.viewmodel.TaskListViewModel

// ── UI State ─────────────────────────────────────────────────────────────────

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedProjectId: String? = null,
    val filterCompleted: Boolean? = null,   // null = all, false = active, true = done
    val sortBy: TaskSort = TaskSort.CREATED_DESC,
    val pendingSyncCount: Int = 0,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
)

// ── UI Events ─────────────────────────────────────────────────────────────────

sealed class TaskListUiEvent {
    data class FilterByCompletion(val completed: Boolean?) : TaskListUiEvent()
    data class SortBy(val sort: TaskSort) : TaskListUiEvent()
    data class SelectProject(val projectId: String) : TaskListUiEvent()
    data class DeleteTask(val taskId: String) : TaskListUiEvent()
    data class CompleteTask(val taskId: String) : TaskListUiEvent()
    object ClearError : TaskListUiEvent()
    object ManualSync : TaskListUiEvent()
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TaskListUiEvent.ClearError)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Task Snap",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (uiState.pendingSyncCount > 0) {
                                Text(
                                    text = "${uiState.pendingSyncCount} pending sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(TaskListUiEvent.ManualSync) }) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = "Sync now",
                                tint = if (uiState.isSyncing)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Offline banner
                AnimatedVisibility(
                    visible = !uiState.isOnline,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut(),
                ) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Offline — changes will sync when connected",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                // Filter chips
                FilterChipRow(
                    filterCompleted = uiState.filterCompleted,
                    onFilter = { viewModel.onEvent(TaskListUiEvent.FilterByCompletion(it)) },
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateTask,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Task") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading && uiState.tasks.isEmpty() -> {
                    // Skeleton / loading — simple centred indicator
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                uiState.tasks.isEmpty() -> {
                    EmptyTasksView(
                        filterCompleted = uiState.filterCompleted,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 96.dp,   // clear FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.tasks.filter { it.syncStatus.name != "PENDING_DELETE" },
                            key = { it.id },
                        ) { task ->
                            TaskCard(
                                task = task,
                                onToggleComplete = {
                                    viewModel.onEvent(TaskListUiEvent.CompleteTask(task.id))
                                },
                                onDelete = {
                                    viewModel.onEvent(TaskListUiEvent.DeleteTask(task.id))
                                },
                                onClick = { onNavigateToTaskDetail(task.id) },
                            )
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }

            // Sync overlay
            AnimatedVisibility(
                visible = uiState.isSyncing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ── Filter chip row ───────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(
    filterCompleted: Boolean?,
    onFilter: (Boolean?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            Triple("All",    null,  filterCompleted == null),
            Triple("Active", false, filterCompleted == false),
            Triple("Done",   true,  filterCompleted == true),
        ).forEach { (label, value, selected) ->
            FilterChip(
                selected = selected,
                onClick = { onFilter(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
