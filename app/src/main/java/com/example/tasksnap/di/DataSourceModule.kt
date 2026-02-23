package com.example.tasksnap.di

import com.example.tasksnap.data.remote.datasource.MockRemoteTaskDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    // LocalTaskDataSource uses constructor injection (@Inject)
    // so we don't need to provide it here—Hilt finds it automatically

    @Provides
    @Singleton
    fun provideMockRemoteTaskDataSource(): MockRemoteTaskDataSource =
        MockRemoteTaskDataSource()
}