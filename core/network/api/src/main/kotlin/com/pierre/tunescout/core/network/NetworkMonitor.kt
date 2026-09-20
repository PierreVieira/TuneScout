package com.pierre.tunescout.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Whether the device currently has a network the app can reach the iTunes API through.
 *
 * A screen reads it to say why it is showing cached data, and to try again by itself once the
 * connection is back, instead of guessing from the last failure it saw.
 */
fun interface NetworkMonitor {
    fun observeIsOnline(): Flow<Boolean>
}
