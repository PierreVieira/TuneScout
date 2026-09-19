package com.pierre.tunescout.feature.library.presentation.model

sealed interface LibrarySearchUiEvent {
    data class OnQueryChanged(
        val query: String,
    ) : LibrarySearchUiEvent

    data object OnClearQueryClicked : LibrarySearchUiEvent

    data class OnItemClicked(
        val item: LibraryItemUiModel,
    ) : LibrarySearchUiEvent

    data class OnRecentSearchRemoved(
        val item: LibraryItemUiModel,
    ) : LibrarySearchUiEvent

    data object OnBackClicked : LibrarySearchUiEvent
}
