package com.example.tasksnap.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Observes device connectivity state.
 * Emits true when online, false when offline.
 */
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun observeIsOnline(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
            ?: run {
                trySend(false)
                close()
                return@callbackFlow
            }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Could be other networks still available
                val isOnline = connectivityManager.activeNetwork != null
                trySend(isOnline)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Initial state
        val isOnline = connectivityManager.activeNetwork != null
        trySend(isOnline)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}