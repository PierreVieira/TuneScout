package com.pierre.tunescout.core.playback

import kotlin.time.Duration

interface TransportControls {
    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun cycleRepeatMode()

    /**
     * Turns shuffle on or off. Turning it on shuffles the context's songs still to come, behind the
     * ones queued by hand; turning it off puts the context back in its own order around the song
     * playing.
     */
    fun toggleShuffle()
}
