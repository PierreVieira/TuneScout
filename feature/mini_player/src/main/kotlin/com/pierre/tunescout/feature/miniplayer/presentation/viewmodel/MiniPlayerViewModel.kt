package com.pierre.tunescout.feature.miniplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

class MiniPlayerViewModel(
    private val observablePlayback: ObservablePlayback,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = MiniPlayerUiState(
        song = null,
        isPlaying = false,
        hasEnded = false,
        progress = 0f,
    )

    val uiState: StateFlow<MiniPlayerUiState> = observablePlayback
        .observePlaybackState()
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: MiniPlayerUiEvent) = when (event) {
        MiniPlayerUiEvent.OnClicked -> openPlayer()
        MiniPlayerUiEvent.OnPlayPauseClicked -> transportControls.togglePlayPause()
        MiniPlayerUiEvent.OnQueueClicked -> navigator.navigate(QueueRoute)
    }

    private fun openPlayer() {
        val songId = uiState.value.song?.id ?: return
        navigator.navigate(PlayerRoute(songId = songId))
    }

    private fun toUiState(playback: PlaybackState): MiniPlayerUiState = MiniPlayerUiState(
        song = playback.currentSong,
        isPlaying = playback.isPlaying,
        hasEnded = playback.hasEnded,
        progress = getProgress(playback),
    )

    private fun getProgress(playback: PlaybackState): Float {
        val duration = playback.duration.takeIf { value -> value > Duration.ZERO }
            ?: playback.currentSong?.duration
            ?: return 0f
        if (duration <= Duration.ZERO) return 0f
        return (playback.position / duration).toFloat().coerceIn(0f, 1f)
    }
}
