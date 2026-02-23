package com.example.tasksnap.presentation.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasksnap.domain.model.Priority
import com.example.tasksnap.presentation.viewmodel.TaskDetailUiEvent
import com.example.tasksnap.presentation.viewmodel.TaskDetailViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = taskId != "new"

    // Navigate back on success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBack()
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TaskDetailUiEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Task" else "New Task",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick = { viewModel.onEvent(TaskDetailUiEvent.Delete) },
                            enabled = !uiState.isDeleting,
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ── Title ─────────────────────────────────────────────────────
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(TaskDetailUiEvent.TitleChanged(it)) },
                    label = { Text("Title") },
                    placeholder = { Text("What needs to be done?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                    isError = uiState.title.isBlank() && uiState.error != null,
                )

                // ── Description ───────────────────────────────────────────────
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(TaskDetailUiEvent.DescriptionChanged(it)) },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Add details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )

                // ── Priority ──────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Priority.entries.forEach { priority ->
                            val selected = uiState.priority == priority
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onEvent(TaskDetailUiEvent.PriorityChanged(priority)) },
                                label = { Text(priority.displayName()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = priority.chipColor(),
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }

                // ── Due Date ──────────────────────────────────────────────────
                DueDatePicker(
                    dueDate = uiState.dueDate,
                    onDateSelected = { viewModel.onEvent(TaskDetailUiEvent.DueDateChanged(it)) },
                )

                Spacer(Modifier.height(8.dp))

                // ── Save button ───────────────────────────────────────────────
                Button(
                    onClick = { viewModel.onEvent(TaskDetailUiEvent.Save) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isSaving && uiState.title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (isEditing) "Save Changes" else "Create Task",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Due Date Picker ───────────────────────────────────────────────────────────

@Composable
private fun DueDatePicker(
    dueDate: LocalDateTime?,
    onDateSelected: (LocalDateTime?) -> Unit,
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Due Date",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = dueDate?.format(dateFormatter) ?: "No due date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dueDate != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row {
                    TextButton(
                        onClick = {
                            val now = dueDate ?: LocalDateTime.now()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    onDateSelected(LocalDateTime.of(year, month + 1, day, 23, 59))
                                },
                                now.year,
                                now.monthValue - 1,
                                now.dayOfMonth,
                            ).show()
                        },
                    ) {
                        Text(if (dueDate == null) "Pick date" else "Change")
                    }

                    if (dueDate != null) {
                        IconButton(onClick = { onDateSelected(null) }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Clear date",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Priority helpers ──────────────────────────────────────────────────────────

private fun Priority.displayName() = when (this) {
    Priority.HIGH   -> "High"
    Priority.MEDIUM -> "Medium"
    Priority.LOW    -> "Low"
}

@Composable
private fun Priority.chipColor() = when (this) {
    Priority.HIGH   -> com.example.tasksnap.presentation.ui.theme.PriorityHigh
    Priority.MEDIUM -> com.example.tasksnap.presentation.ui.theme.PriorityMedium
    Priority.LOW    -> com.example.tasksnap.presentation.ui.theme.PriorityLow
}
