package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiState {
    data object Loading : CollectionUiState

    data class Loaded(
        val title: CollectionTitle,
        val songs: List<Song>,
        val nowPlayingId: Long?,
        val isDeletable: Boolean,
    ) : CollectionUiState
}
