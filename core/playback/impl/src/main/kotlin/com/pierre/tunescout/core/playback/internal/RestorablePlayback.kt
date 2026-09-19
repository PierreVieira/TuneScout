package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.PlaybackSession

internal fun interface RestorablePlayback {
    fun restore(session: PlaybackSession)
}
