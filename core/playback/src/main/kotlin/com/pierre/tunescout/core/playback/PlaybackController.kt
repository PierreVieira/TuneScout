package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface PlaybackController {
    val state: StateFlow<PlaybackState>

    fun play(
        song: Song,
        songs: List<Song>,
        context: PlaybackContext,
    )

    fun addToQueue(songs: List<Song>)

    fun removeFromQueue(entryId: String)

    fun moveInQueue(
        fromIndex: Int,
        toIndex: Int,
    )

    fun skipTo(entryId: String)

    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeat()
}
