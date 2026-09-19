package com.pierre.tunescout.feature.library.presentation.model

sealed interface CreatePlaylistUiEvent {
    data class OnNameChanged(
        val name: String,
    ) : CreatePlaylistUiEvent

    data object OnConfirmClicked : CreatePlaylistUiEvent

    data object OnDismissed : CreatePlaylistUiEvent
}
