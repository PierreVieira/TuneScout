package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.PlayableSongs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
) : PlayableSongs,
    ObservablePlayableSongs {
    /**
     * Started optimistically, like the screens: the monitor reports the real state as soon as it is
     * collected, and a tap in the meantime is better sent to the player than refused.
     */
    private val isOnline: StateFlow<Boolean> = networkMonitor
        .observeIsOnline()
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = true)

    override fun isPlayable(song: Song): Boolean = isPlayable(song = song, isOnline = isOnline.value)

    /**
     * Each answer is tied to the connection state it was built on, so a list never draws one
     * connection's reach while the monitor has already reported another.
     *
     * @return the player's reach, re-answered every time the connection comes or goes.
     */
    override fun observePlayableSongs(): Flow<PlayableSongs> = isOnline.map { isOnline ->
        PlayableSongs { song -> isPlayable(song = song, isOnline = isOnline) }
    }

    private fun isPlayable(
        song: Song,
        isOnline: Boolean,
    ): Boolean = isOnline || previewCache.isCached(song)
}
