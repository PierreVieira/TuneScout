package com.pierre.tunescout.feature.miniplayer.presentation.model

import com.pierre.tunescout.core.model.Song

data class MiniPlayerUiState(
    val song: Song?,
    val isPlaying: Boolean,
    val hasEnded: Boolean,
    val progress: Float,
)
