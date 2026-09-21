package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.feature.songoptions.presentation.content.SongOptionsContent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.screenshotfixtures.getLucky
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class SongOptionsScreenshots : ReadmeScreenshotsTest() {
    private val playerBehindSheet = PlayerUiState.Loaded(
        song = getLucky,
        status = PlaybackStatus.Paused,
        position = 18.seconds,
        duration = 29.seconds,
        repeatMode = RepeatMode.Off,
        isShuffleEnabled = false,
        hasPrevious = true,
        hasNext = true,
    )

    @Test
    fun options() {
        capture(
            fileName = "options",
            title = "Queue it, or open its album",
            description = "The same sheet from every list and from the player",
        ) {
            SheetOverScreen(
                screen = { PlayerContent(uiState = playerBehindSheet, isSideBySide = false, onEvent = {}) },
                sheet = {
                    SongOptionsContent(
                        uiState = SongOptionsUiState(song = getLucky, isFavorite = false),
                        onEvent = {},
                    )
                },
            )
        }
    }
}
