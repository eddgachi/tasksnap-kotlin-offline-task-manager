package com.example.tasksnap.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tasksnap.domain.model.Priority
import com.example.tasksnap.domain.model.SyncStatus
import com.example.tasksnap.domain.model.Task
import com.example.tasksnap.presentation.ui.theme.PriorityHigh
import com.example.tasksnap.presentation.ui.theme.PriorityLow
import com.example.tasksnap.presentation.ui.theme.PriorityMedium
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val priorityColor = task.priority.color()
    val isCompleted = task.isCompleted
    val alpha = if (isCompleted) 0.55f else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            // ── Priority accent bar ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)          // matches min card height
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(priorityColor.copy(alpha = alpha)),
            )

            // ── Content ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 0.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Priority chip
                    PriorityChip(priority = task.priority, alpha = alpha)

                    // Due date
                    task.dueDate?.let { due ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = dueDateColor(due, isCompleted).copy(alpha = alpha),
                            )
                            Text(
                                text = due.format(DateTimeFormatter.ofPattern("MMM d")),
                                style = MaterialTheme.typography.labelSmall,
                                color = dueDateColor(due, isCompleted).copy(alpha = alpha),
                            )
                        }
                    }

                    // Sync dot
                    if (task.syncStatus == SyncStatus.PENDING_SYNC) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                        )
                    }
                }
            }

            // ── Trailing actions ────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
            ) {
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        imageVector = if (isCompleted)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (isCompleted) "Mark incomplete" else "Mark complete",
                        tint = if (isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.alpha(0.65f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// ── Priority chip ─────────────────────────────────────────────────────────────

@Composable
private fun PriorityChip(priority: Priority, alpha: Float = 1f) {
    val color = priority.color()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f * alpha)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = priority.label(),
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = alpha),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun dueDateColor(due: LocalDateTime, completed: Boolean): Color {
    if (completed) return MaterialTheme.colorScheme.onSurfaceVariant
    val now = LocalDateTime.now()
    return when {
        due.isBefore(now) -> MaterialTheme.colorScheme.error
        due.isBefore(now.plusDays(2)) -> PriorityMedium
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun Priority.color(): Color = when (this) {
    Priority.HIGH   -> PriorityHigh
    Priority.MEDIUM -> PriorityMedium
    Priority.LOW    -> PriorityLow
}

private fun Priority.label(): String = when (this) {
    Priority.HIGH   -> "HIGH"
    Priority.MEDIUM -> "MED"
    Priority.LOW    -> "LOW"
}
