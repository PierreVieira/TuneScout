package com.pierre.tunescout.core.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val NO_QUEUE_INDEX = -1

/**
 * The player as the app sees it.
 *
 * @property entries the queue, in the order it plays — shuffled already while shuffle is on.
 * @property currentIndex where [entries] is, or [NO_QUEUE_INDEX] when it is empty.
 * @property context what the queue was started from.
 * @property status whether the player is playing, and why not when it is not.
 * @property position how far into the current song the player is.
 * @property duration how long the current song is, once the player has buffered it.
 * @property repeatMode what the player does once the queue runs out.
 * @property isShuffleEnabled whether the context's songs play in a random order.
 * @property unshuffledOrder the ids of the context's entries in the order the context gave them,
 * kept while shuffle is on so that turning it off can put them back. Empty while it is off.
 */
data class PlaybackState(
    val entries: List<QueueEntry>,
    val currentIndex: Int,
    val context: PlaybackContext?,
    val status: PlaybackStatus,
    val position: Duration,
    val duration: Duration,
    val repeatMode: RepeatMode,
    val isShuffleEnabled: Boolean,
    val unshuffledOrder: List<String>,
) {
    val currentEntry: QueueEntry?
        get() = entries.getOrNull(currentIndex)

    val currentSong: Song?
        get() = currentEntry?.song

    val isPlaying: Boolean
        get() = status == PlaybackStatus.Playing

    val hasEnded: Boolean
        get() = status == PlaybackStatus.Ended

    /**
     * The song the player is still on. Once it ends, the player keeps it around so it can be
     * replayed, but nothing is playing any more.
     */
    val nowPlayingSong: Song?
        get() = currentSong.takeUnless { hasEnded }

    val nowPlaying: NowPlaying?
        get() = nowPlayingSong?.let { song -> NowPlaying(songId = song.id, isPlaying = isPlaying) }

    /**
     * The player only learns how long a song is once it has buffered it, so the song's own
     * duration stands in until then — which is what keeps a progress bar from starting at zero
     * length every time a song begins.
     */
    val totalDuration: Duration
        get() = duration.takeIf { value -> value > Duration.ZERO } ?: currentSong?.duration ?: Duration.ZERO

    /** Repeating the whole queue makes its first and last songs neighbours. */
    private val wrapsAround: Boolean
        get() = repeatMode == RepeatMode.All && currentIndex >= 0 && entries.isNotEmpty()

    val hasPrevious: Boolean
        get() = currentIndex > 0 || wrapsAround

    /**
     * The entry asking for the previous song would move to, and nothing while it would start the
     * current one over — which is what it does once the song is past [previousSongWindow], the way
     * every player behaves.
     */
    val previousEntry: QueueEntry?
        get() = entries
            .getOrNull(if (currentIndex == 0 && wrapsAround) entries.lastIndex else currentIndex - 1)
            .takeIf { position <= previousSongWindow }

    val hasNext: Boolean
        get() = currentIndex >= 0 && (currentIndex < entries.lastIndex || wrapsAround)

    val upcomingEntries: List<QueueEntry>
        get() = if (currentIndex < 0) emptyList() else entries.drop(currentIndex + 1)

    /**
     * The context's own songs don't count: a song coming up again because the album or playlist
     * holds it is not the user adding it twice.
     *
     * @return whether the user already queued the song and it has yet to play.
     */
    fun isQueuedByUser(songId: Long): Boolean = upcomingEntries.any { entry ->
        entry.source == QueueSource.UserQueue && entry.song.id == songId
    }

    companion object {
        /**
         * How far into a song asking for the previous one still means the song before it. The
         * player is built with this same window, so both answer the button the same way.
         */
        val previousSongWindow: Duration = 3.seconds

        val Idle: PlaybackState = PlaybackState(
            entries = emptyList(),
            currentIndex = NO_QUEUE_INDEX,
            context = null,
            status = PlaybackStatus.Idle,
            position = Duration.ZERO,
            duration = Duration.ZERO,
            repeatMode = RepeatMode.Off,
            isShuffleEnabled = false,
            unshuffledOrder = emptyList(),
        )
    }
}
