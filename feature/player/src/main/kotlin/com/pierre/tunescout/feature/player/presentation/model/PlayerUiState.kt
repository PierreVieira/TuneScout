package com.pierre.tunescout.feature.player.presentation.model

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration

sealed interface PlayerUiState {
    data object Loading : PlayerUiState

    data object NotFound : PlayerUiState

    /** The pane beside the tabs, before anything has been played. */
    data object NothingPlaying : PlayerUiState

    data class Loaded(
        val song: Song,
        val status: PlaybackStatus,
        val position: Duration,
        val duration: Duration,
        val repeatMode: RepeatMode,
        val isShuffleEnabled: Boolean,
        val hasPrevious: Boolean,
        val hasNext: Boolean,
    ) : PlayerUiState {
        val isPlaying: Boolean
            get() = status == PlaybackStatus.Playing

        val hasEnded: Boolean
            get() = status == PlaybackStatus.Ended

        val progress: Float
            get() = if (duration > Duration.ZERO) {
                (position / duration).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }
    }
}
