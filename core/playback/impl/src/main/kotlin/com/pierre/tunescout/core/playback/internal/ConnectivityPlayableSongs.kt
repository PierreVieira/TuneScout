package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.playback.ObservableDownloads
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.PlayableSongs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Follows the connection for as long as the app runs, so a tap is answered at once instead of
 * waiting for the monitor.
 *
 * @property previewCache the previews already played on the device.
 * @param networkMonitor where the connection state comes from.
 * @param observableDownloads the songs the user downloaded, which play offline even once the
 * played previews are gone.
 * @param scope the playback scope the connection is followed in.
 */
internal class ConnectivityPlayableSongs(
    private val previewCache: PreviewCache,
    networkMonitor: NetworkMonitor,
    observableDownloads: ObservableDownloads,
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

    private val downloadStatuses: StateFlow<Map<Long, SongDownloadStatus>> = observableDownloads
        .observeDownloadStatuses()
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = emptyMap())

    override fun isPlayable(song: Song): Boolean =
        isPlayable(song = song, isOnline = isOnline.value, downloadStatuses = downloadStatuses.value)

    /**
     * Each answer is tied to the connection state and the downloads it was built on, so a list never
     * draws one connection's reach while the monitor has already reported another.
     *
     * @return the player's reach, re-answered every time the connection comes or goes, and every
     * time a download arrives or is removed.
     */
    override fun observePlayableSongs(): Flow<PlayableSongs> =
        combine(isOnline, downloadStatuses) { isOnline, statuses ->
            PlayableSongs { song -> isPlayable(song = song, isOnline = isOnline, downloadStatuses = statuses) }
        }

    private fun isPlayable(
        song: Song,
        isOnline: Boolean,
        downloadStatuses: Map<Long, SongDownloadStatus>,
    ): Boolean = isOnline || downloadStatuses[song.id] == SongDownloadStatus.Downloaded || previewCache.isCached(song)
}
