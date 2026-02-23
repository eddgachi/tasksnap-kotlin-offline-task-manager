package com.example.tasksnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val color: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncStatus: String,            // "PENDING_SYNC", "SYNCED", "PENDING_DELETE"
)

fun com.example.tasksnap.domain.model.Project.toEntity(): ProjectEntity = ProjectEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    color = this.color,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    syncStatus = this.syncStatus.name,
)

fun ProjectEntity.toDomain(): com.example.tasksnap.domain.model.Project =
    com.example.tasksnap.domain.model.Project(
        id = this.id,
        name = this.name,
        description = this.description,
        color = this.color,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        syncStatus = com.example.tasksnap.domain.model.SyncStatus.valueOf(this.syncStatus),
    )