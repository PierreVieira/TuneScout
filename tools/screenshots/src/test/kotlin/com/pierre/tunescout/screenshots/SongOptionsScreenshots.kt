package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
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
        isFavorite = false,
    )

    @Test
    fun options() {
        capture(
            fileName = "options",
            title = "Queue it, or open its album",
            description = "The same sheet everywhere, minus liking here: the player's bar already carries the heart",
        ) {
            SheetOverScreen(
                screen = { PlayerContent(uiState = playerBehindSheet, layout = PlayerLayout.Stacked, onEvent = {}) },
                sheet = {
                    SongOptionsContent(
                        uiState = SongOptionsUiState(
                            song = getLucky,
                            isFavorite = false,
                            isFavoriteVisible = false,
                            isRemovableFromPlaylist = false,
                            isReorderable = false,
                            isDownloaded = false,
                            duplicatePlacement = null,
                        ),
                        onEvent = {},
                    )
                },
            )
        }
    }
}
