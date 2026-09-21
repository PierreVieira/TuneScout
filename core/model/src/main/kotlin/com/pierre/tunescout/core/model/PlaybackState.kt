package com.pierre.tunescout.core.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val NO_QUEUE_INDEX = -1

data class PlaybackState(
    val entries: List<QueueEntry>,
    val currentIndex: Int,
    val context: PlaybackContext?,
    val status: PlaybackStatus,
    val position: Duration,
    val duration: Duration,
    val isRepeatEnabled: Boolean,
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

    val hasPrevious: Boolean
        get() = currentIndex > 0

    /**
     * The entry asking for the previous song would move to, and nothing while it would start the
     * current one over — which is what it does once the song is past [previousSongWindow], the way
     * every player behaves.
     */
    val previousEntry: QueueEntry?
        get() = entries
            .getOrNull(currentIndex - 1)
            .takeIf { position <= previousSongWindow }

    val hasNext: Boolean
        get() = currentIndex >= 0 && currentIndex < entries.lastIndex

    val upcomingEntries: List<QueueEntry>
        get() = if (currentIndex < 0) emptyList() else entries.drop(currentIndex + 1)

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
            isRepeatEnabled = false,
        )
    }
}
