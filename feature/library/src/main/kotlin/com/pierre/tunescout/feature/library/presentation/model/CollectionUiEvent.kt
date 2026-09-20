package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongRemoved(
        val song: Song,
    ) : CollectionUiEvent

    data object OnPlayNowClicked : CollectionUiEvent

    data object OnMoreClicked : CollectionUiEvent

    data object OnBackClicked : CollectionUiEvent
}
