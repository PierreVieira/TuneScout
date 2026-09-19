package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.Song

data class SongOptionsUiState(
    val song: Song?,
    val isRecentlyPlayed: Boolean,
)
