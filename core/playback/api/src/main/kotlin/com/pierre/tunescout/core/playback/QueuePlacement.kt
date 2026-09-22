package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.Song

/** Where in the queue songs were asked to go. */
enum class QueuePlacement {
    /** Right after the song playing. */
    Next,

    /** After everything the user queued before them. */
    End,
}

/**
 * Songs the user already queued and asked to queue again, while the screen asks whether to.
 *
 * @property placement where they were asked to go.
 * @property count how many of the songs asked for are already queued.
 */
data class DuplicatesInQueue(
    val placement: QueuePlacement,
    val count: Int,
)

fun Enqueuer.enqueue(
    songs: List<Song>,
    placement: QueuePlacement,
) = when (placement) {
    QueuePlacement.Next -> queueNext(songs)
    QueuePlacement.End -> addToQueue(songs)
}
