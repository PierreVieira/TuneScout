package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.playback.PlayableSongs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Follows the connection for as long as the app runs, so a tap is answered at once instead of
 * waiting for the monitor.
 *
 * @property previewCache the previews already on the device.
 * @param networkMonitor where the connection state comes from.
 * @param scope the playback scope the connection is followed in.
 */
internal class ConnectivityPlayableSongs(
    private val previewCache: PreviewCache,
    networkMonitor: NetworkMonitor,
    scope: CoroutineScope,
) : PlayableSongs {
    /**
     * Started optimistically, like the screens: the monitor reports the real state as soon as it is
     * collected, and a tap in the meantime is better sent to the player than refused.
     */
    private val isOnline: StateFlow<Boolean> = networkMonitor
        .observeIsOnline()
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = true)

    override fun isPlayable(song: Song): Boolean = isOnline.value || previewCache.isCached(song)
}
