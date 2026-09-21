package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface AlbumUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : AlbumUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : AlbumUiEvent

    data class OnSongSwipedToQueue(
        val song: Song,
    ) : AlbumUiEvent

    data class OnSongSwipedToFavorite(
        val song: Song,
    ) : AlbumUiEvent

    data object OnPlayPauseClicked : AlbumUiEvent

    data object OnShuffleClicked : AlbumUiEvent

    data object OnFavoriteClicked : AlbumUiEvent

    data object OnMoreClicked : AlbumUiEvent

    data object OnRetryClicked : AlbumUiEvent

    data object OnBackClicked : AlbumUiEvent
}
