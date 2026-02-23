package com.example.tasksnap.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Workers are created by androidx.hilt.work.HiltWorkerFactory (injected via
 * TaskSnapApplication.workerFactory). No manual WorkManager setup needed here.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule
