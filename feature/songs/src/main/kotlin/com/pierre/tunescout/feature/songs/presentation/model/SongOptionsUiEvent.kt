package com.pierre.tunescout.feature.songs.presentation.model

sealed interface SongOptionsUiEvent {
    data object OnPlayNextClicked : SongOptionsUiEvent

    data object OnAddToQueueClicked : SongOptionsUiEvent

    data object OnViewAlbumClicked : SongOptionsUiEvent

    data object OnDismissed : SongOptionsUiEvent
}
