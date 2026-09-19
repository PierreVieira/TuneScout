package com.pierre.tunescout.core.playback

import kotlin.time.Duration

interface TransportControls {
    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeat()
}
