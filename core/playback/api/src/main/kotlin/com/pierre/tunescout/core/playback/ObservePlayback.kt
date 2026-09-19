package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow

fun interface ObservePlayback {
    fun observePlaybackState(): StateFlow<PlaybackState>
}
