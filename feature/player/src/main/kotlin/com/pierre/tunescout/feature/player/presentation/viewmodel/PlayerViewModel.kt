package com.pierre.tunescout.feature.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
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
    private val playbackController: PlaybackController,
    private val navigator: Navigator,
) : ViewModel() {
    val uiState: StateFlow<PlayerUiState> = combine(observeSong(route.songId), playbackController.state, ::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlayerUiState.Loading)

    fun onEvent(event: PlayerUiEvent) = when (event) {
        PlayerUiEvent.OnPlayPauseClicked -> togglePlayPause()
        is PlayerUiEvent.OnSeekFinished -> playbackController.seekTo(event.position)
        PlayerUiEvent.OnSkipNextClicked -> playbackController.skipToNext()
        PlayerUiEvent.OnSkipPreviousClicked -> playbackController.skipToPrevious()
        PlayerUiEvent.OnRepeatClicked -> playbackController.toggleRepeat()
        PlayerUiEvent.OnBackClicked -> navigator.navigateBack()
        PlayerUiEvent.OnMoreClicked -> navigateToOptions()
    }

    private fun togglePlayPause() {
        val shownSong = (uiState.value as? PlayerUiState.Loaded)?.song ?: return
        if (playbackController.state.value.currentSong
                ?.id == shownSong.id
        ) {
            playbackController.togglePlayPause()
        } else {
            playbackController.play(
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
