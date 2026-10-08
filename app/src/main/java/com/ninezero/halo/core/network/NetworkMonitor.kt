package com.ninezero.halo.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 네트워크 끊김 뒤 다시 연결 알림 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 등록 때 오는 현재 연결은 거름, 재조회 무한 반복 방지
    val restored: Flow<Unit> = callbackFlow {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        var sawLoss = connectivityManager.activeNetwork == null
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                sawLoss = true
            }

            override fun onAvailable(network: Network) {
                if (sawLoss) {
                    sawLoss = false
                    trySend(Unit)
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
