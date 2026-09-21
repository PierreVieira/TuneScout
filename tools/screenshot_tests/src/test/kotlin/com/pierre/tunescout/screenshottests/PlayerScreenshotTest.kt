package com.pierre.tunescout.screenshottests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class PlayerScreenshotTest : ScreenshotTest() {
    /** About half of a phone on its side, which is what the pane beside the tabs gets there. */
    private val compactPaneWidth = 400.dp

    private val loaded = PlayerUiState.Loaded(
        song = getLucky,
        status = PlaybackStatus.Playing,
        position = 18.seconds,
        duration = 29.seconds,
        repeatMode = RepeatMode.Off,
        isShuffleEnabled = false,
        hasPrevious = true,
        hasNext = true,
    )

    @Test
    fun loading() {
        snapshot(name = "loading") {
            PlayerContent(uiState = PlayerUiState.Loading, layout = PlayerLayout.Stacked, onEvent = {})
        }
    }

    @Test
    fun notFound() {
        snapshot(name = "not_found") {
            PlayerContent(uiState = PlayerUiState.NotFound, layout = PlayerLayout.Stacked, onEvent = {})
        }
    }

    @Test
    fun loaded() {
        snapshot(name = "loaded", variants = ScreenshotVariant.all) {
            PlayerContent(uiState = loaded, layout = PlayerLayout.Stacked, onEvent = {})
        }
    }

    @Test
    fun endedWithRepeat() {
        snapshot(name = "ended_with_repeat") {
            PlayerContent(
                uiState = loaded.copy(
                    status = PlaybackStatus.Ended,
                    position = 29.seconds,
                    repeatMode = RepeatMode.One,
                    hasNext = false,
                ),
                layout = PlayerLayout.Stacked,
                onEvent = {},
            )
        }
    }

    @Test
    fun shuffledRepeatingTheQueue() {
        snapshot(name = "shuffled_repeating_queue") {
            PlayerContent(
                uiState = loaded.copy(repeatMode = RepeatMode.All, isShuffleEnabled = true),
                layout = PlayerLayout.Stacked,
                onEvent = {},
            )
        }
    }

    /** Half a phone on its side, the pane beside the tabs: the controls come before the artwork. */
    @Test
    fun loadedCompact() {
        snapshot(name = "loaded_compact", variants = ScreenshotVariant.all, isLandscape = true) {
            Box(modifier = Modifier.width(compactPaneWidth)) {
                PlayerContent(uiState = loaded, layout = PlayerLayout.Compact, hasBack = false, onEvent = {})
            }
        }
    }

    @Test
    fun loadingCompact() {
        snapshot(name = "loading_compact", isLandscape = true) {
            Box(modifier = Modifier.width(compactPaneWidth)) {
                PlayerContent(
                    uiState = PlayerUiState.Loading,
                    layout = PlayerLayout.Compact,
                    hasBack = false,
                    onEvent = {},
                )
            }
        }
    }

    @Test
    fun loadedSideBySide() {
        snapshot(name = "loaded_side_by_side", isLandscape = true) {
            PlayerContent(uiState = loaded, layout = PlayerLayout.SideBySide, onEvent = {})
        }
    }
}
