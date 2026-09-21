package com.pierre.tunescout.feature.album.presentation.model

sealed interface AlbumOptionsUiEvent {
    data object OnPlayNextClicked : AlbumOptionsUiEvent

    data object OnAddToQueueClicked : AlbumOptionsUiEvent

    data object OnReorderClicked : AlbumOptionsUiEvent
}
