package com.pierre.tunescout.core.model

import kotlin.time.Duration

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

    val hasNext: Boolean
        get() = currentIndex >= 0 && currentIndex < entries.lastIndex

    val upcomingEntries: List<QueueEntry>
        get() = if (currentIndex < 0) emptyList() else entries.drop(currentIndex + 1)

    companion object {
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
