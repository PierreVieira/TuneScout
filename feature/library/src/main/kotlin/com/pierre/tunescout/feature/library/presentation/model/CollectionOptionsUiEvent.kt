package com.pierre.tunescout.feature.library.presentation.model

sealed interface CollectionOptionsUiEvent {
    data object OnPlayNextClicked : CollectionOptionsUiEvent

    data object OnAddToQueueClicked : CollectionOptionsUiEvent

    data object OnDuplicatesInQueueConfirmed : CollectionOptionsUiEvent

    data object OnDuplicatesInQueueDismissed : CollectionOptionsUiEvent

    data object OnReorderClicked : CollectionOptionsUiEvent

    data object OnDeleteClicked : CollectionOptionsUiEvent

    data object OnDeleteConfirmed : CollectionOptionsUiEvent

    data object OnDeleteDismissed : CollectionOptionsUiEvent
}
