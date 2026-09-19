package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

sealed interface LibraryUiEvent {
    data class OnItemClicked(
        val item: LibraryItemUiModel,
    ) : LibraryUiEvent

    data object OnSearchClicked : LibraryUiEvent

    data object OnCreatePlaylistClicked : LibraryUiEvent

    data class OnViewModeSelected(
        val viewMode: LibraryViewMode,
    ) : LibraryUiEvent
}
