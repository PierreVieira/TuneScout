package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.NowPlaying

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object Error : AlbumUiState

    data class Loaded(
        val album: Album,
        val nowPlaying: NowPlaying?,
        val isFavorite: Boolean,
    ) : AlbumUiState
}
