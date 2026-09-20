package com.pierre.tunescout.feature.miniplayer.presentation.model

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song

/**
 * [Empty] is the player with no song, which has no status or progress to speak of. What the button
 * shows comes from the one [Loaded.status], so "playing and ended" cannot be written down.
 */
sealed interface MiniPlayerUiState {
    data object Empty : MiniPlayerUiState

    data class Loaded(
        val song: Song,
        val status: PlaybackStatus,
        val progress: Float,
    ) : MiniPlayerUiState {
        val isPlaying: Boolean
            get() = status == PlaybackStatus.Playing

        val hasEnded: Boolean
            get() = status == PlaybackStatus.Ended
    }
}
