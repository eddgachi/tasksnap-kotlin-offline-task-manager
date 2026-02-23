package com.example.tasksnap.di

import com.example.tasksnap.data.repository.TaskRepositoryImpl
import com.example.tasksnap.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bind the repository interface to its implementation.
 * Now, when a class needs TaskRepository, Hilt injects TaskRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository
}
