package com.pierre.tunescout.feature.library.presentation.model

sealed interface LibraryUiEvent {
    data class OnItemClicked(
        val item: LibraryItemUiModel,
    ) : LibraryUiEvent

    data object OnSearchClicked : LibraryUiEvent

    data object OnCreatePlaylistClicked : LibraryUiEvent

    data object OnViewModeToggled : LibraryUiEvent
}
