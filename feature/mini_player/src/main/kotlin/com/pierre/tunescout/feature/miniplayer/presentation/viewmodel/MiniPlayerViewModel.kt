package com.pierre.tunescout.feature.miniplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiAction
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

class MiniPlayerViewModel(
    private val transportControls: TransportControls,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
) : ViewModel() {
    val uiAction: SharedFlow<MiniPlayerUiAction>
        field = MutableSharedFlow<MiniPlayerUiAction>()

    val uiState: StateFlow<MiniPlayerUiState> = observablePlayback
        .observePlaybackState()
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MiniPlayerUiState.Empty)

    fun onEvent(event: MiniPlayerUiEvent) = when (event) {
        MiniPlayerUiEvent.OnClicked -> openPlayer()
        MiniPlayerUiEvent.OnPlayPauseClicked -> togglePlayPause()
        MiniPlayerUiEvent.OnQueueClicked -> navigator.navigate(QueueRoute)
    }

    /**
     * Pausing is always honoured; starting is not. The song the bar is on may have become one the
     * player cannot reach — the connection went away while it was paused — and starting it then
     * would leave the bar stuck on a song that never plays.
     */
    private fun togglePlayPause() {
        val loaded = uiState.value as? MiniPlayerUiState.Loaded ?: return
        if (loaded.isPlaying) return transportControls.togglePlayPause()
        if (!playableSongs.isPlayable(loaded.song)) return showSongUnavailableOffline()
        transportControls.togglePlayPause()
    }

    private fun showSongUnavailableOffline() {
        emitAction(MiniPlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: MiniPlayerUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun openPlayer() {
        val loaded = uiState.value as? MiniPlayerUiState.Loaded ?: return
        navigator.navigate(PlayerRoute(songId = loaded.song.id))
    }

    private fun toUiState(playback: PlaybackState): MiniPlayerUiState {
        val song = playback.currentSong ?: return MiniPlayerUiState.Empty
        return MiniPlayerUiState.Loaded(
            song = song,
            status = playback.status,
            progress = getProgress(playback),
        )
    }

    private fun getProgress(playback: PlaybackState): Float {
        val duration = playback.totalDuration
        if (duration <= Duration.ZERO) return 0f
        return (playback.position / duration).toFloat().coerceIn(0f, 1f)
    }
}
