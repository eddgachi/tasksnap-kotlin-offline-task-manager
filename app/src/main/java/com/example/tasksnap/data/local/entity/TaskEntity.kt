package com.example.tasksnap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["syncStatus"]),
        Index(value = ["dueDate"]),
        Index(value = ["isCompleted"]),
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,                    // UUID, not auto-generated
    val projectId: String,             // Foreign key to projects table
    val title: String,
    val description: String,
    val dueDate: LocalDateTime?,
    val priority: String,              // "LOW", "MEDIUM", "HIGH"
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,      // UTC
    val updatedAt: LocalDateTime,      // UTC
    val syncStatus: String,            // "PENDING_SYNC", "SYNCED", "PENDING_DELETE"
)

// Extension function to convert domain → entity
fun com.example.tasksnap.domain.model.Task.toEntity(): TaskEntity = TaskEntity(
    id = this.id,
    projectId = this.projectId,
    title = this.title,
    description = this.description,
    dueDate = this.dueDate,
    priority = this.priority.name,
    isCompleted = this.isCompleted,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    syncStatus = this.syncStatus.name,
)

// Extension function to convert entity → domain
fun TaskEntity.toDomain(): com.example.tasksnap.domain.model.Task =
    com.example.tasksnap.domain.model.Task(
        id = this.id,
        projectId = this.projectId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        priority = com.example.tasksnap.domain.model.Priority.valueOf(this.priority),
        isCompleted = this.isCompleted,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        syncStatus = com.example.tasksnap.domain.model.SyncStatus.valueOf(this.syncStatus),
    )