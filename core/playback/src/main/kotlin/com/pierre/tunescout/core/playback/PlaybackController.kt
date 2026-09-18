package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface PlaybackController {
    val state: StateFlow<PlaybackState>

    fun play(
        song: Song,
        queue: List<Song>,
    )

    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeat()
}
