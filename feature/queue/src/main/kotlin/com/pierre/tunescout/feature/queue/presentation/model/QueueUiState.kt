package com.pierre.tunescout.feature.queue.presentation.model

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry

data class QueueUiState(
    val contextTitle: String?,
    val nowPlaying: QueueEntry?,
    val status: PlaybackStatus,
    val queuedByUser: List<QueueEntry>,
    val upNext: List<QueueEntry>,
) {
    val isPlaying: Boolean
        get() = status == PlaybackStatus.Playing

    val hasEnded: Boolean
        get() = status == PlaybackStatus.Ended

    val isEmpty: Boolean
        get() = nowPlaying == null && queuedByUser.isEmpty() && upNext.isEmpty()
}
