package com.pierre.tunescout.core.playback.internal

import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.NO_QUEUE_INDEX
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.utils.IdGenerator
import kotlin.time.Duration

internal class PlaybackQueue(
    private val player: ExoPlayer,
    private val mediaItemFactory: MediaItemFactory,
    private val idGenerator: IdGenerator,
) {
    var entries: List<QueueEntry> = emptyList()
        private set

    val currentIndex: Int
        get() = if (player.mediaItemCount == 0) NO_QUEUE_INDEX else player.currentMediaItemIndex

    val isEmpty: Boolean
        get() = entries.isEmpty()

    fun startContext(
        song: Song,
        songs: List<Song>,
    ) {
        val timeline = buildTimeline(
            songs = songs.ifEmpty { listOf(song) },
            startSongId = song.id,
            carriedEntries = getCarriedEntries(entries, currentIndex),
            createEntryId = idGenerator::createId,
        )
        replaceWith(timeline.entries, timeline.startIndex, 0L)
    }

    fun restore(
        restoredEntries: List<QueueEntry>,
        currentEntryId: String?,
        position: Duration,
    ) {
        val startIndex = restoredEntries
            .indexOfFirst { entry -> entry.id == currentEntryId }
            .coerceAtLeast(0)
        replaceWith(restoredEntries, startIndex, position.inWholeMilliseconds)
    }

    fun queueNext(songs: List<Song>) {
        insertAt(songs, currentIndex + 1)
    }

    fun addToQueue(songs: List<Song>) {
        insertAt(songs, getUserQueueInsertIndex(entries, currentIndex))
    }

    fun remove(entryId: String) {
        val index = findIndex(entryId)
        if (index < 0) return
        entries = entries.filterIndexed { position, _ -> position != index }
        player.removeMediaItem(index)
    }

    fun move(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex !in entries.indices || toIndex !in entries.indices || fromIndex == toIndex) return
        entries = entries.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        player.moveMediaItem(fromIndex, toIndex)
    }

    fun findIndex(entryId: String): Int = entries.indexOfFirst { entry -> entry.id == entryId }

    private fun insertAt(
        songs: List<Song>,
        index: Int,
    ) {
        val added = buildEntries(songs, QueueSource.UserQueue, idGenerator::createId)
        entries = entries.take(index) + added + entries.drop(index)
        player.addMediaItems(index, added.map(mediaItemFactory::createMediaItem))
    }

    private fun replaceWith(
        newEntries: List<QueueEntry>,
        startIndex: Int,
        positionMillis: Long,
    ) {
        entries = newEntries
        player.setMediaItems(newEntries.map(mediaItemFactory::createMediaItem), startIndex, positionMillis)
    }
}
