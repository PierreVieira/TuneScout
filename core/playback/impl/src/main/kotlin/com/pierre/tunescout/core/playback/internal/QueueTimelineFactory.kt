package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.utils.IdGenerator
import kotlin.random.Random

/**
 * The ordering rules of the queue. Songs queued by hand always play before the rest of the context,
 * shuffled or not: shuffle only ever reorders the context, the way Spotify does.
 *
 * @property idGenerator where each new entry's id comes from.
 * @property random what the context is shuffled with.
 */
internal class QueueTimelineFactory(
    private val idGenerator: IdGenerator,
    private val random: Random,
) {
    /**
     * @return [songs] as a new context starting at [startSongId] — or at its first song, or a random
     * one while [isShuffled], when there is none — with [carriedEntries] queued right behind it.
     * Shuffled, the start comes first and the rest of the context follows in a random order.
     */
    fun buildTimeline(
        songs: List<Song>,
        startSongId: Long?,
        carriedEntries: List<QueueEntry>,
        isShuffled: Boolean,
    ): QueueTimeline {
        val contextEntries = buildEntries(songs, QueueSource.Context)
        val startIndex = findStartIndex(contextEntries, startSongId, isShuffled)
        if (!isShuffled) {
            return QueueTimeline(
                entries = contextEntries.take(startIndex + 1) + carriedEntries + contextEntries.drop(startIndex + 1),
                startIndex = startIndex,
            )
        }
        val start = contextEntries[startIndex]
        return QueueTimeline(
            entries =
                listOf(start) + carriedEntries + contextEntries.filter { entry -> entry != start }.shuffled(random),
            startIndex = 0,
            unshuffledOrder = contextEntries.map(QueueEntry::id),
        )
    }

    fun buildEntries(
        songs: List<Song>,
        source: QueueSource,
    ): List<QueueEntry> = songs.map { song -> QueueEntry(id = idGenerator.createId(), song = song, source = source) }

    fun getCarriedEntries(
        entries: List<QueueEntry>,
        currentIndex: Int,
    ): List<QueueEntry> = entries
        .drop(currentIndex + 1)
        .filter { entry -> entry.source == QueueSource.UserQueue }

    fun getUserQueueInsertIndex(
        entries: List<QueueEntry>,
        currentIndex: Int,
    ): Int {
        val firstUpcoming = currentIndex + 1
        val queued = entries.drop(firstUpcoming).takeWhile { entry -> entry.source == QueueSource.UserQueue }
        return firstUpcoming + queued.size
    }

    /**
     * What has played stays where it is, so the previous button still walks back through it; what
     * is still to come is the songs queued by hand, in their order, then the rest of the context in
     * a random one.
     *
     * @return [entries] with the context after [currentIndex] shuffled, and the context's order.
     */
    fun buildShuffled(
        entries: List<QueueEntry>,
        currentIndex: Int,
    ): QueueTimeline {
        val (queued, context) = entries
            .drop(currentIndex + 1)
            .partition { entry -> entry.source == QueueSource.UserQueue }
        return QueueTimeline(
            entries = entries.take(currentIndex + 1) + queued + context.shuffled(random),
            startIndex = currentIndex,
            unshuffledOrder = entries.filter { entry -> entry.source == QueueSource.Context }.map(QueueEntry::id),
        )
    }

    /**
     * The context goes back to [unshuffledOrder] and carries on from where the song playing sits in
     * it, the way Spotify does: its songs before that one become what has played, and the ones after
     * it what is still to come — behind the songs queued by hand, which keep their place. A song
     * queued by hand that is playing sits where the last context song heard before it does.
     *
     * @return [entries] with the context in [unshuffledOrder] around the entry at [currentIndex].
     */
    fun buildUnshuffled(
        entries: List<QueueEntry>,
        currentIndex: Int,
        unshuffledOrder: List<String>,
    ): QueueTimeline {
        val current = entries.getOrNull(currentIndex) ?: return QueueTimeline(entries, currentIndex)
        val context = getContextInOrder(entries, unshuffledOrder)
        val anchor = entries.take(currentIndex + 1).lastOrNull { entry -> entry.source == QueueSource.Context }
        val anchorPosition = context.indexOf(anchor)
        val played = entries.take(currentIndex).filter { entry -> entry.source == QueueSource.UserQueue } +
            context.take(anchorPosition + 1).filter { entry -> entry != current }
        val queued = entries.drop(currentIndex + 1).filter { entry -> entry.source == QueueSource.UserQueue }
        return QueueTimeline(
            entries = played + current + queued + context.drop(anchorPosition + 1),
            startIndex = played.size,
        )
    }

    private fun findStartIndex(
        contextEntries: List<QueueEntry>,
        startSongId: Long?,
        isShuffled: Boolean,
    ): Int = when {
        startSongId != null -> contextEntries.indexOfFirst { entry -> entry.song.id == startSongId }.coerceAtLeast(0)
        isShuffled -> random.nextInt(contextEntries.size)
        else -> 0
    }

    /**
     * @return the context entries of [entries] in [unshuffledOrder], with any entry the order does
     * not know about kept at the end.
     */
    private fun getContextInOrder(
        entries: List<QueueEntry>,
        unshuffledOrder: List<String>,
    ): List<QueueEntry> {
        val context = entries.filter { entry -> entry.source == QueueSource.Context }
        val positions = unshuffledOrder.withIndex().associate { (position, id) -> id to position }
        return context.sortedBy { entry -> positions[entry.id] ?: Int.MAX_VALUE }
    }
}
