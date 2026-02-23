package com.example.tasksnap.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tasksnap.domain.usecase.SyncTasksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import timber.log.Timber

/**
 * Background worker that syncs pending tasks with the server.
 * @HiltWorker lets androidx.hilt.work.HiltWorkerFactory inject dependencies.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTasksUseCase: SyncTasksUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        Timber.d("SyncWorker: starting sync (attempt $runAttemptCount)")
        val count = syncTasksUseCase()
        Timber.d("SyncWorker: synced $count items")
        Result.success()
    } catch (e: CancellationException) {
        Timber.w("SyncWorker: cancelled")
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) {
            Timber.w(e, "SyncWorker: error, will retry")
            Result.retry()
        } else {
            Timber.e(e, "SyncWorker: giving up after $runAttemptCount attempts")
            Result.failure()
        }
    }
}
