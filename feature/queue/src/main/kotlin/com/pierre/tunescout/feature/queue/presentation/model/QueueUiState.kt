package com.pierre.tunescout.feature.queue.presentation.model

import com.pierre.tunescout.core.model.QueueEntry

data class QueueUiState(
    val contextTitle: String?,
    val nowPlaying: QueueEntry?,
    val queuedByUser: List<QueueEntry>,
    val upNext: List<QueueEntry>,
) {
    val isEmpty: Boolean
        get() = nowPlaying == null && queuedByUser.isEmpty() && upNext.isEmpty()
}
