package com.pierre.tunescout.ui.component

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

enum class PlayButtonState {
    Play,
    Pause,
    Replay,
    ;

    companion object {
        fun of(
            isPlaying: Boolean,
            hasEnded: Boolean,
        ): PlayButtonState = when {
            isPlaying -> Pause
            hasEnded -> Replay
            else -> Play
        }
    }
}

val PlayButtonState.icon: ImageVector
    get() = when (this) {
        PlayButtonState.Play -> TuneScoutIcons.play
        PlayButtonState.Pause -> TuneScoutIcons.pause
        PlayButtonState.Replay -> TuneScoutIcons.replay
    }

@get:StringRes
val PlayButtonState.contentDescription: Int
    get() = when (this) {
        PlayButtonState.Play -> R.string.ui_play
        PlayButtonState.Pause -> R.string.ui_pause
        PlayButtonState.Replay -> R.string.ui_replay
    }
