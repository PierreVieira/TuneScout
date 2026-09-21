package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.QueueEntry

/**
 * A queue ready to hand to the player.
 *
 * @property entries the queue, in the order it plays.
 * @property startIndex the entry of [entries] the player is on.
 * @property unshuffledOrder the ids of the context's entries in the context's own order, while the
 * queue is shuffled. Empty when it is not.
 */
internal data class QueueTimeline(
    val entries: List<QueueEntry>,
    val startIndex: Int,
    val unshuffledOrder: List<String> = emptyList(),
)
