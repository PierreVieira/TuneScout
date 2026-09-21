package com.pierre.tunescout.feature.queue.presentation.model

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry

/**
 * @property contextTitle what the queue is playing from, when it came from an album.
 * @property nowPlaying the entry playing right now, when there is one.
 * @property status what the player is doing with it.
 * @property queuedByUser the entries the user put next, in order.
 * @property upNext what follows them, from whatever the queue was built on.
 * @property unplayableSongIds which of the queued songs the player cannot reach right now — an
 * entry queued while online whose preview never reached the device. Their rows are drawn dimmer,
 * so a tap that is refused is seen coming.
 */
data class QueueUiState(
    val contextTitle: String?,
    val nowPlaying: QueueEntry?,
    val status: PlaybackStatus,
    val queuedByUser: List<QueueEntry>,
    val upNext: List<QueueEntry>,
    val unplayableSongIds: Set<Long>,
) {
    val isPlaying: Boolean
        get() = status == PlaybackStatus.Playing

    val hasEnded: Boolean
        get() = status == PlaybackStatus.Ended

    val isEmpty: Boolean
        get() = nowPlaying == null && queuedByUser.isEmpty() && upNext.isEmpty()
}
