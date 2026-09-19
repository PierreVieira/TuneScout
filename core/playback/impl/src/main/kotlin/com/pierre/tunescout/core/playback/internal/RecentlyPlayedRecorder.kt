package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class RecentlyPlayedRecorder(
    private val playbackState: Flow<PlaybackState>,
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
) {
    fun start(scope: CoroutineScope) {
        playbackState
            .map { state -> state.currentSong.takeIf { state.isPlaying } }
            .filterNotNull()
            .distinctUntilChanged { previous, next -> previous.id == next.id }
            .onEach(::record)
            .launchIn(scope)
    }

    private suspend fun record(song: Song) {
        recentlyPlayedLocalDataSource.record(song)
    }
}
