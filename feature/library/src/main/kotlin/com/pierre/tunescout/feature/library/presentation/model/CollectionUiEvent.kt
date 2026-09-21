package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongSwipedToQueue(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongSwipedToFavorite(
        val song: Song,
    ) : CollectionUiEvent

    data object OnPlayPauseClicked : CollectionUiEvent

    data object OnShuffleClicked : CollectionUiEvent

    data object OnMoreClicked : CollectionUiEvent

    data object OnBackClicked : CollectionUiEvent
}
