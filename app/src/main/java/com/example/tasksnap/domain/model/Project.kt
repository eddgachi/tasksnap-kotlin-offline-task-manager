package com.example.tasksnap.domain.model

import java.time.LocalDateTime

/**
 * A Project groups related Tasks.
 * Example: "Work", "Personal", "Learning"
 */
data class Project(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String = "#2196F3",  // Hex color for UI
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncStatus: SyncStatus,
)