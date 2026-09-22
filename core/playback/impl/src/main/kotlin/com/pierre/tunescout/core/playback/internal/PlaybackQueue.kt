package com.pierre.tunescout.core.playback.internal

import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.NO_QUEUE_INDEX
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration

internal class PlaybackQueue(
    private val player: ExoPlayer,
    private val mediaItemFactory: MediaItemFactory,
    private val timelineFactory: QueueTimelineFactory,
) {
    var entries: List<QueueEntry> = emptyList()
        private set

    /** Whether the context's songs are in a random order, which is the player's shuffle mode once it syncs. */
    var isShuffled: Boolean = false
        private set

    /** The context's own order while [isShuffled], so turning shuffle off can put it back. */
    var unshuffledOrder: List<String> = emptyList()
        private set

    val currentIndex: Int
        get() = if (player.mediaItemCount == 0) NO_QUEUE_INDEX else player.currentMediaItemIndex

    val isEmpty: Boolean
        get() = entries.isEmpty()

    fun startContext(
        song: Song,
        songs: List<Song>,
    ) {
        startContext(songs = songs.ifEmpty { listOf(song) }, startSongId = song.id)
    }

    /** Starts [songs] from their first song, or from a random one while [isShuffled]. */
    fun startContextFromTop(songs: List<Song>) {
        startContext(songs = songs, startSongId = null)
    }

    fun restore(
        restoredEntries: List<QueueEntry>,
        currentEntryId: String?,
        position: Duration,
        isShuffled: Boolean,
        unshuffledOrder: List<String>,
    ) {
        val startIndex = restoredEntries
            .indexOfFirst { entry -> entry.id == currentEntryId }
            .coerceAtLeast(0)
        this.isShuffled = isShuffled
        this.unshuffledOrder = if (isShuffled) unshuffledOrder else emptyList()
        replaceWith(restoredEntries, startIndex, position.inWholeMilliseconds)
    }

    fun shuffle() {
        if (isShuffled) return
        isShuffled = true
        rearrange(timelineFactory.buildShuffled(entries, currentIndex))
    }

    fun unshuffle() {
        if (!isShuffled) return
        isShuffled = false
        rearrange(timelineFactory.buildUnshuffled(entries, currentIndex, unshuffledOrder))
    }

    fun playNow(songs: List<Song>) {
        val index = currentIndex + 1
        insertAt(songs, index)
        player.seekTo(index, 0L)
    }

    fun queueNext(songs: List<Song>) {
        insertAt(songs, currentIndex + 1)
    }

    fun addToQueue(songs: List<Song>) {
        insertAt(songs, timelineFactory.getUserQueueInsertIndex(entries, currentIndex))
    }

    fun remove(entryId: String) {
        val index = findIndex(entryId)
        if (index < 0) return
        entries = entries.filterIndexed { position, _ -> position != index }
        player.removeMediaItem(index)
    }

    /** Drops every entry but the one at [currentIndex], which keeps playing undisturbed. */
    fun clear() {
        val index = currentIndex
        if (index < 0) return
        entries = entries.filterIndexed { position, _ -> position == index }
        player.removeMediaItems(index + 1, player.mediaItemCount)
        player.removeMediaItems(0, index)
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
        val added = timelineFactory.buildEntries(songs, QueueSource.UserQueue)
        entries = entries.take(index) + added + entries.drop(index)
        player.addMediaItems(index, added.map(mediaItemFactory::createMediaItem))
    }

    private fun startContext(
        songs: List<Song>,
        startSongId: Long?,
    ) {
        val timeline = timelineFactory.buildTimeline(
            songs = songs,
            startSongId = startSongId,
            carriedEntries = timelineFactory.getCarriedEntries(entries, currentIndex),
            isShuffled = isShuffled,
        )
        unshuffledOrder = timeline.unshuffledOrder
        replaceWith(timeline.entries, timeline.startIndex, 0L)
    }

    /**
     * Puts [timeline] in place around the entry playing, which it keeps at [QueueTimeline.startIndex]:
     * the songs before and after it are replaced, never the song itself, so it plays on without a
     * gap.
     */
    private fun rearrange(timeline: QueueTimeline) {
        val index = currentIndex
        unshuffledOrder = timeline.unshuffledOrder
        entries = timeline.entries
        if (index < 0) return
        val upcoming = timeline.entries.drop(timeline.startIndex + 1)
        val played = timeline.entries.take(timeline.startIndex)
        player.replaceMediaItems(index + 1, player.mediaItemCount, upcoming.map(mediaItemFactory::createMediaItem))
        player.replaceMediaItems(0, index, played.map(mediaItemFactory::createMediaItem))
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
