package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.NowPlaying

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object Error : AlbumUiState

    /**
     * @property album the album and its tracks, as the device has them.
     * @property nowPlaying which track is playing, when one of them is.
     * @property isFavorite whether the album is in the user's library.
     * @property isStale the album is the one on the device and the call meant to confirm it
     * failed. The rows stay on screen — they are what the user came for — and the screen says so
     * instead of pretending they are fresh.
     */
    data class Loaded(
        val album: Album,
        val nowPlaying: NowPlaying?,
        val isFavorite: Boolean,
        val isStale: Boolean,
    ) : AlbumUiState
}
