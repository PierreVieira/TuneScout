package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class PlayerScreenshotTest : ScreenshotTest() {
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
            PlayerContent(uiState = PlayerUiState.Loading, isSideBySide = false, onEvent = {})
        }
    }

    @Test
    fun notFound() {
        snapshot(name = "not_found") {
            PlayerContent(uiState = PlayerUiState.NotFound, isSideBySide = false, onEvent = {})
        }
    }

    @Test
    fun loaded() {
        snapshot(name = "loaded", variants = ScreenshotVariant.all) {
            PlayerContent(uiState = loaded, isSideBySide = false, onEvent = {})
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
                isSideBySide = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun shuffledRepeatingTheQueue() {
        snapshot(name = "shuffled_repeating_queue") {
            PlayerContent(
                uiState = loaded.copy(repeatMode = RepeatMode.All, isShuffleEnabled = true),
                isSideBySide = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun loadedSideBySide() {
        snapshot(name = "loaded_side_by_side", isLandscape = true) {
            PlayerContent(uiState = loaded, isSideBySide = true, onEvent = {})
        }
    }
}
