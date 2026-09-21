package com.pierre.tunescout.feature.widget.domain.usecase.impl

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.utils.DispatcherProvider
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import com.pierre.tunescout.feature.widget.domain.usecase.ControlWidgetPlayback
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

class ControlWidgetPlaybackUseCase(
    private val observablePlayback: ObservablePlayback,
    private val transportControls: TransportControls,
    private val playbackStarter: PlaybackStarter,
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val restoreTimeout: Duration,
) : ControlWidgetPlayback {
    override suspend fun togglePlayPause() {
        onRestoredPlayer(transportControls::togglePlayPause)
    }

    override suspend fun skipToNext() {
        onRestoredPlayer(transportControls::skipToNext)
    }

    override suspend fun skipToPrevious() {
        onRestoredPlayer(transportControls::skipToPrevious)
    }

    override suspend fun playSong(songId: Long) {
        val songs = recentlyPlayedLocalDataSource.observe(WidgetState.SHORTCUT_COUNT).first()
        val song = songs.firstOrNull { recent -> recent.id == songId } ?: return
        withContext(dispatcherProvider.main) {
            playbackStarter.play(song = song, songs = songs, context = PlaybackContext.SingleSong)
        }
    }

    /**
     * Holds the tap until the saved queue is back in the player, so the first thing a cold process
     * does is not to skip a queue that is still empty. A player that stays empty — nothing was ever
     * played — falls through once [restoreTimeout] is up rather than dropping the tap silently.
     */
    private suspend fun onRestoredPlayer(control: () -> Unit) {
        withTimeoutOrNull(restoreTimeout) {
            observablePlayback.observePlaybackState().first { state -> state.entries.isNotEmpty() }
        }
        withContext(dispatcherProvider.main) { control() }
    }
}
