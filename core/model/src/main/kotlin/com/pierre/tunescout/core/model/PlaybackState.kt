package com.pierre.tunescout.core.model

import kotlin.time.Duration

data class PlaybackState(
    val currentSong: Song?,
    val queue: List<Song>,
    val status: PlaybackStatus,
    val position: Duration,
    val duration: Duration,
    val isRepeatEnabled: Boolean,
) {
    val isPlaying: Boolean
        get() = status == PlaybackStatus.Playing

    companion object {
        val Idle: PlaybackState = PlaybackState(
            currentSong = null,
            queue = emptyList(),
            status = PlaybackStatus.Idle,
            position = Duration.ZERO,
            duration = Duration.ZERO,
            isRepeatEnabled = false,
        )
    }
}
