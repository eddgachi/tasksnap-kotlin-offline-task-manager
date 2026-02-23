package com.example.tasksnap.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    // ConnectivityObserver is provided via @Inject constructor — no explicit @Provides needed.
}
