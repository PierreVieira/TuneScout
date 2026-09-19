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
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.player.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlayerViewModel(
    private val route: PlayerRoute,
    observeSong: ObserveSong,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
) : ViewModel() {
    private val currentSong: Song?
        get() = observablePlayback.observePlaybackState().value.currentSong

    val uiState: StateFlow<PlayerUiState> = combine(
        observeSong(route.songId),
        observablePlayback.observePlaybackState(),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlayerUiState.Loading)

    fun onEvent(event: PlayerUiEvent) = when (event) {
        PlayerUiEvent.OnPlayPauseClicked -> togglePlayPause()
        is PlayerUiEvent.OnSeekFinished -> transportControls.seekTo(event.position)
        PlayerUiEvent.OnSkipNextClicked -> transportControls.skipToNext()
        PlayerUiEvent.OnSkipPreviousClicked -> transportControls.skipToPrevious()
        PlayerUiEvent.OnRepeatClicked -> transportControls.toggleRepeat()
        PlayerUiEvent.OnQueueClicked -> navigator.navigate(QueueRoute)
        PlayerUiEvent.OnBackClicked -> navigator.navigateBack()
        PlayerUiEvent.OnMoreClicked -> navigateToOptions()
    }

    private fun togglePlayPause() {
        val shownSong = (uiState.value as? PlayerUiState.Loaded)?.song ?: return
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
