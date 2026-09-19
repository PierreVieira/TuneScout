package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow

internal interface RestorablePlayback {
    val state: StateFlow<PlaybackState>

    fun restore(session: PlaybackSession)
}
