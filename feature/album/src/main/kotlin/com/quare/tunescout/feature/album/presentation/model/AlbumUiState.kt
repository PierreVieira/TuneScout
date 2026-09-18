package com.quare.tunescout.feature.album.presentation.model

import com.quare.tunescout.core.model.Album

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object Error : AlbumUiState

    data class Loaded(
        val album: Album,
        val nowPlayingId: Long?,
        val isPlaying: Boolean,
    ) : AlbumUiState
}
