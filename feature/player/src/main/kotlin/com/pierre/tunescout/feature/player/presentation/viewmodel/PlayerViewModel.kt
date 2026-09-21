package com.pierre.tunescout.feature.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.player.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiAction
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val route: PlayerRoute,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val playableSongs: PlayableSongs,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
    observeSong: ObserveSong,
) : ViewModel() {
    private val playback: PlaybackState
        get() = observablePlayback.observePlaybackState().value

    private val currentSong: Song?
        get() = playback.currentSong

    /** The song the player would move on to, and nothing while the queue ends on the current one. */
    private val nextSong: Song?
        get() = playback.upcomingEntries.firstOrNull()?.song

    val uiAction: SharedFlow<PlayerUiAction>
        field = MutableSharedFlow<PlayerUiAction>()

    val uiState: StateFlow<PlayerUiState> = combine(
        observeSong(route.songId),
        observablePlayback.observePlaybackState(),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlayerUiState.Loading)

    fun onEvent(event: PlayerUiEvent) = when (event) {
        PlayerUiEvent.OnPlayPauseClicked -> togglePlayPause()
        is PlayerUiEvent.OnSeekFinished -> transportControls.seekTo(event.position)
        PlayerUiEvent.OnSkipNextClicked -> skipToNext()
        PlayerUiEvent.OnSkipPreviousClicked -> transportControls.skipToPrevious()
        PlayerUiEvent.OnRepeatClicked -> transportControls.toggleRepeat()
        PlayerUiEvent.OnQueueClicked -> navigator.navigate(QueueRoute)
        PlayerUiEvent.OnBackClicked -> navigator.navigateBack()
        PlayerUiEvent.OnMoreClicked -> navigateToOptions()
    }

    /**
     * Pausing is always honoured; starting is not. A song the player cannot reach is refused with a
     * message, whether it would be started from the beginning or resumed where it stopped.
     */
    private fun togglePlayPause() {
        val shownSong = (uiState.value as? PlayerUiState.Loaded)?.song ?: return
        if (playback.isPlaying) return transportControls.togglePlayPause()
        if (!playableSongs.isPlayable(shownSong)) return showSongUnavailableOffline()
        if (currentSong?.id == shownSong.id) {
            transportControls.togglePlayPause()
        } else {
            playbackStarter.play(
                song = shownSong,
                songs = listOf(shownSong),
                context = PlaybackContext.SingleSong,
            )
        }
    }

    /** The next song was queued while the player could reach it, which it may no longer be able to. */
    private fun skipToNext() {
        val next = nextSong ?: return transportControls.skipToNext()
        if (!playableSongs.isPlayable(next)) return showSongUnavailableOffline()
        transportControls.skipToNext()
    }

    private fun showSongUnavailableOffline() {
        emitAction(PlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: PlayerUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun navigateToOptions() {
        val shownSongId = (uiState.value as? PlayerUiState.Loaded)?.song?.id ?: route.songId
        navigator.navigate(SongOptionsRoute(songId = shownSongId))
    }

    private fun toUiState(
        routeSong: Song?,
        playback: PlaybackState,
    ): PlayerUiState {
        val song = playback.currentSong ?: routeSong ?: return PlayerUiState.NotFound
        val isCurrent = playback.currentSong?.id == song.id
        return PlayerUiState.Loaded(
            song = song,
            status = playback.status,
            position = if (isCurrent) playback.position else PlaybackState.Idle.position,
            duration = if (isCurrent &&
                playback.duration > PlaybackState.Idle.duration
            ) {
                playback.duration
            } else {
                song.duration
            },
            isRepeatEnabled = playback.isRepeatEnabled,
            hasPrevious = playback.hasPrevious,
            hasNext = playback.hasNext,
        )
    }
}
