package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song

internal data class QueueTimeline(
    val entries: List<QueueEntry>,
    val startIndex: Int,
)

internal fun buildTimeline(
    songs: List<Song>,
    startSongId: Long,
    carriedEntries: List<QueueEntry>,
    createEntryId: () -> String,
): QueueTimeline {
    val contextEntries = buildEntries(songs, QueueSource.Context, createEntryId)
    val startIndex = contextEntries.indexOfFirst { entry -> entry.song.id == startSongId }.coerceAtLeast(0)
    return QueueTimeline(
        entries = contextEntries.take(startIndex + 1) + carriedEntries + contextEntries.drop(startIndex + 1),
        startIndex = startIndex,
    )
}

internal fun buildEntries(
    songs: List<Song>,
    source: QueueSource,
    createEntryId: () -> String,
): List<QueueEntry> = songs.map { song -> QueueEntry(id = createEntryId(), song = song, source = source) }

internal fun getCarriedEntries(
    entries: List<QueueEntry>,
    currentIndex: Int,
): List<QueueEntry> = entries
    .drop(currentIndex + 1)
    .filter { entry -> entry.source == QueueSource.UserQueue }

internal fun getUserQueueInsertIndex(
    entries: List<QueueEntry>,
    currentIndex: Int,
): Int {
    val firstUpcoming = currentIndex + 1
    val queued = entries.drop(firstUpcoming).takeWhile { entry -> entry.source == QueueSource.UserQueue }
    return firstUpcoming + queued.size
}
