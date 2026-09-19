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

    val hasPrevious: Boolean
        get() = currentIndex > 0

    val hasNext: Boolean
        get() = currentIndex >= 0 && currentIndex < entries.lastIndex

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
