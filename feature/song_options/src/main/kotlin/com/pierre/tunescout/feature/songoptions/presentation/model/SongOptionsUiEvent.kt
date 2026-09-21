package com.pierre.tunescout.feature.songoptions.presentation.model

sealed interface SongOptionsUiEvent {
    data object OnFavoriteClicked : SongOptionsUiEvent

    data object OnAddToPlaylistClicked : SongOptionsUiEvent

    data object OnPlayNowClicked : SongOptionsUiEvent

    data object OnPlayNextClicked : SongOptionsUiEvent

    data object OnAddToQueueClicked : SongOptionsUiEvent

    data object OnViewAlbumClicked : SongOptionsUiEvent

    data object OnRemoveFromPlaylistClicked : SongOptionsUiEvent

    data object OnDismissed : SongOptionsUiEvent
}
