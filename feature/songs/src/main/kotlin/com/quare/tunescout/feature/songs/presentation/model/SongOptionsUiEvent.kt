package com.quare.tunescout.feature.songs.presentation.model

sealed interface SongOptionsUiEvent {
    data object OnViewAlbumClicked : SongOptionsUiEvent

    data object OnDismissed : SongOptionsUiEvent
}
