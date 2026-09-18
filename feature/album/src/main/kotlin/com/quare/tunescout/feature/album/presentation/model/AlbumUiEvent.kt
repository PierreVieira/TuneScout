package com.quare.tunescout.feature.album.presentation.model

import com.quare.tunescout.core.model.Song

sealed interface AlbumUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : AlbumUiEvent

    data object OnRetryClicked : AlbumUiEvent

    data object OnBackClicked : AlbumUiEvent
}
