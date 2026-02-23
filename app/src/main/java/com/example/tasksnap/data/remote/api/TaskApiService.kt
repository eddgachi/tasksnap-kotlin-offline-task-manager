package com.example.tasksnap.data.remote.api

import kotlinx.serialization.Serializable


/**
 * Retrofit interface (TODO: implement with real endpoints).
 *
 * For now, we use MockRemoteTaskDataSource.
 * When the backend is ready:
 * 1. Define endpoints here
 * 2. Create data transfer objects (DTOs)
 * 3. Replace mock calls with real Retrofit calls
 *
 * This is a placeholder to show the intended structure.
 */

// TODO: Define API DTOs
@Serializable
data class TaskDto(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val dueDate: String?,  // ISO 8601 string
    val priority: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

// TODO: Define Retrofit service
// @Suppress("UNUSED")
// interface TaskApiService {
//     @POST("/api/tasks")
//     suspend fun createTask(@Body task: TaskDto): TaskDto
//
//     @GET("/api/tasks/{id}")
//     suspend fun getTask(@Path("id") id: String): TaskDto
//
//     @PUT("/api/tasks/{id}")
//     suspend fun updateTask(@Path("id") id: String, @Body task: TaskDto): TaskDto
//
//     @DELETE("/api/tasks/{id}")
//     suspend fun deleteTask(@Path("id") id: String)
// }