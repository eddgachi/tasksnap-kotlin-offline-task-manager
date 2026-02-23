package com.example.tasksnap.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tasksnap.domain.model.SyncStatus

/**
 * Visual indicator showing sync status of a task.
 *
 * - SYNCED: Green checkmark
 * - PENDING_SYNC: Blue cloud icon (uploading)
 * - PENDING_DELETE: Red warning icon
 */
@Composable
fun SyncStatusIndicator(syncStatus: SyncStatus) {
    when (syncStatus) {
        SyncStatus.SYNCED -> {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Green, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Synced",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        SyncStatus.PENDING_SYNC -> {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Blue, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Syncing...",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        SyncStatus.PENDING_DELETE -> {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Red, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Pending delete",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}