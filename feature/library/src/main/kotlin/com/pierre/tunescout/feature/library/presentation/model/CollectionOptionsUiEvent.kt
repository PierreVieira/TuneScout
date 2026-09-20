package com.pierre.tunescout.feature.library.presentation.model

sealed interface CollectionOptionsUiEvent {
    data object OnPlayNextClicked : CollectionOptionsUiEvent

    data object OnAddToQueueClicked : CollectionOptionsUiEvent

    data object OnDeleteClicked : CollectionOptionsUiEvent

    data object OnDeleteConfirmed : CollectionOptionsUiEvent

    data object OnDeleteDismissed : CollectionOptionsUiEvent
}
