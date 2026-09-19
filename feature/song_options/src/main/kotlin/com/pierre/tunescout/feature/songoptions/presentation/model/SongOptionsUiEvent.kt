package com.pierre.tunescout.feature.songoptions.presentation.model

sealed interface SongOptionsUiEvent {
    data object OnFavoriteClicked : SongOptionsUiEvent

    data object OnAddToPlaylistClicked : SongOptionsUiEvent

    data object OnPlayNextClicked : SongOptionsUiEvent

    data object OnAddToQueueClicked : SongOptionsUiEvent

    data object OnViewAlbumClicked : SongOptionsUiEvent

    data object OnRemoveFromRecentlyPlayedClicked : SongOptionsUiEvent

    data object OnDismissed : SongOptionsUiEvent
}
