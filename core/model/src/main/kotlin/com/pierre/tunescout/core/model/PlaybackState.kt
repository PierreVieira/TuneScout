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

    /**
     * The player only learns how long a song is once it has buffered it, so the song's own
     * duration stands in until then — which is what keeps a progress bar from starting at zero
     * length every time a song begins.
     */
    val totalDuration: Duration
        get() = duration.takeIf { value -> value > Duration.ZERO } ?: currentSong?.duration ?: Duration.ZERO

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
