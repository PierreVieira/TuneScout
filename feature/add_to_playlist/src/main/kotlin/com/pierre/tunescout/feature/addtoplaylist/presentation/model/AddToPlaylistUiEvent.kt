package com.pierre.tunescout.feature.addtoplaylist.presentation.model

sealed interface AddToPlaylistUiEvent {
    data class OnPlaylistClicked(
        val playlistId: Long,
    ) : AddToPlaylistUiEvent

    data object OnNewPlaylistClicked : AddToPlaylistUiEvent

    data class OnNewPlaylistNameChanged(
        val name: String,
    ) : AddToPlaylistUiEvent

    data object OnNewPlaylistConfirmed : AddToPlaylistUiEvent

    data object OnNewPlaylistDismissed : AddToPlaylistUiEvent

    data object OnDismissed : AddToPlaylistUiEvent
}
