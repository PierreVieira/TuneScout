package com.pierre.tunescout.feature.queue.presentation.model

sealed interface QueueUiEvent {
    data object OnNowPlayingClicked : QueueUiEvent

    data class OnEntryClicked(
        val entryId: String,
    ) : QueueUiEvent

    data class OnRemoveClicked(
        val entryId: String,
    ) : QueueUiEvent

    data class OnEntryMoved(
        val fromEntryId: String,
        val toEntryId: String,
    ) : QueueUiEvent

    data object OnClearQueueClicked : QueueUiEvent

    data object OnClearQueueConfirmed : QueueUiEvent

    data object OnClearQueueDismissed : QueueUiEvent
}
