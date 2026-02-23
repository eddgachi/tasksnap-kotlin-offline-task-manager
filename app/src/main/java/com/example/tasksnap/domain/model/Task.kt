package com.example.tasksnap.domain.model

import java.time.LocalDateTime

/**
 * Core domain model for a Task.
 * This is what the app understands as a "Task" at the business level.
 * It doesn't know about Room, Retrofit, or how it's persisted.
 */
data class Task(
    val id: String,                    // Unique identifier
    val projectId: String,             // Which project it belongs to
    val title: String,                 // Required
    val description: String,           // Optional detail
    val dueDate: LocalDateTime?,       // When it's due (nullable)
    val priority: Priority,            // Enum: LOW, MEDIUM, HIGH
    val isCompleted: Boolean,          // Is this task done?
    val createdAt: LocalDateTime,      // Audit timestamp
    val updatedAt: LocalDateTime,      // Last modification
    val syncStatus: SyncStatus,        // PENDING_SYNC, SYNCED, PENDING_DELETE
)

/**
 * Enum to enforce priority values at the domain level.
 * This prevents invalid states (e.g., priority = "URGENT").
 */
enum class Priority {
    LOW, MEDIUM, HIGH
}

/**
 * Sync status tells us whether this task is:
 * - PENDING_SYNC: created/modified locally, needs to send to server
 * - SYNCED: safe on both local and remote
 * - PENDING_DELETE: marked for deletion, needs to delete on server
 */
enum class SyncStatus {
    PENDING_SYNC, SYNCED, PENDING_DELETE
}