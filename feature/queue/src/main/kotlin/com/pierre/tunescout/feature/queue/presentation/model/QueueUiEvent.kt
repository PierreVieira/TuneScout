package com.pierre.tunescout.feature.queue.presentation.model

sealed interface QueueUiEvent {
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
}
