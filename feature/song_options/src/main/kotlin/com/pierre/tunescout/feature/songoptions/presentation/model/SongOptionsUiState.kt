package com.pierre.tunescout.feature.songoptions.presentation.model

import com.pierre.tunescout.core.model.Song

data class SongOptionsUiState(
    val song: Song?,
    val isRecentlyPlayed: Boolean,
    val isFavorite: Boolean,
    val isConfirmingRemoval: Boolean,
)
