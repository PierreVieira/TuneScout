package com.pierre.tunescout.screenshottests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerContent
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.instantCrush
import com.pierre.tunescout.ui.component.PlayButtonState
import org.junit.Test

internal class MiniPlayerScreenshotTest : ScreenshotTest() {
    @Test
    fun playing() {
        snapshot(name = "playing", variants = ScreenshotVariant.all) {
            MiniPlayerBox(song = getLucky, playButtonState = PlayButtonState.Pause)
        }
    }

    @Test
    fun paused() {
        snapshot(name = "paused") {
            MiniPlayerBox(song = instantCrush, playButtonState = PlayButtonState.Play)
        }
    }

    /** The song played to its end, so the button replays it instead of resuming. */
    @Test
    fun ended() {
        snapshot(name = "ended") {
            MiniPlayerBox(song = getLucky, playButtonState = PlayButtonState.Replay, progress = 1f)
        }
    }

    @Composable
    private fun MiniPlayerBox(
        song: Song,
        playButtonState: PlayButtonState,
        progress: Float = RESTORED_PROGRESS,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            MiniPlayerContent(
                song = song,
                playButtonState = playButtonState,
                progress = progress,
                onEvent = {},
            )
        }
    }

    private companion object {
        const val RESTORED_PROGRESS = 0.62f
    }
}
