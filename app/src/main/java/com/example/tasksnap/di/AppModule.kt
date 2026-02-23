package com.example.tasksnap.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * App-level Hilt module.
 * ConnectivityObserver uses @ApplicationContext directly via constructor injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
