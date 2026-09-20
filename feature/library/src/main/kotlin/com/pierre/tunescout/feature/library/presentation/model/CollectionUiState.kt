package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiState {
    data object Loading : CollectionUiState

    data class Loaded(
        val title: CollectionTitle,
        val songs: List<Song>,
        val nowPlaying: NowPlaying?,
        val isDeletable: Boolean,
        val songPendingRemoval: Song?,
    ) : CollectionUiState
}
