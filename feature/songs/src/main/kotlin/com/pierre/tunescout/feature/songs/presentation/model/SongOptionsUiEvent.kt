package com.pierre.tunescout.feature.songs.presentation.model

sealed interface SongOptionsUiEvent {
    data object OnViewAlbumClicked : SongOptionsUiEvent

    data object OnDismissed : SongOptionsUiEvent
}
