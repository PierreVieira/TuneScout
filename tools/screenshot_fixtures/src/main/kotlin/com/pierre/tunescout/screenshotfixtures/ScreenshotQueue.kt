package com.pierre.tunescout.screenshotfixtures

import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song

/** @return an entry the album or playlist being played put in the queue. */
fun contextEntry(song: Song): QueueEntry = QueueEntry(
    id = "context-${song.id}",
    song = song,
    source = QueueSource.Context,
)

/** @return an entry the user queued by hand, which plays before the rest of the context. */
fun userEntry(song: Song): QueueEntry = QueueEntry(
    id = "queued-${song.id}",
    song = song,
    source = QueueSource.UserQueue,
)
