package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class PlayerScreenshots : ReadmeScreenshotsTest() {
    private val nowPlaying = PlayerUiState.Loaded(
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
    fun player() {
        capture(
            fileName = "player",
            title = "A player, and the queue behind it",
            description = "Scrub, repeat, skip, and open the queue from the button on the right",
        ) {
            PlayerContent(isSideBySide = false, uiState = nowPlaying, onEvent = {})
        }
    }
}
