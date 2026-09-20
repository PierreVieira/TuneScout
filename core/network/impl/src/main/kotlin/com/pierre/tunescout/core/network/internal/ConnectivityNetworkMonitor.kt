package com.pierre.tunescout.core.network.internal

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.pierre.tunescout.core.network.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

internal class ConnectivityNetworkMonitor(
    private val connectivityManager: ConnectivityManager,
) : NetworkMonitor {
    /**
     * The default network is the one the HTTP client would use, so a callback on it answers
     * "can this app reach the internet" without tracking every interface the device has.
     *
     * @return the state the device is in when collection starts, and every change after it.
     */
    override fun observeIsOnline(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }
        trySend(isOnline)
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged().conflate()

    /**
     * `VALIDATED` and not only `INTERNET`: the capability is granted once the system has actually
     * reached something through the network, which rules out a hotel portal that answers every
     * request with its own login page.
     */
    private val isOnline: Boolean
        get() = connectivityManager
            .activeNetwork
            ?.let { network -> connectivityManager.getNetworkCapabilities(network) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
}
