package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

private val nowPlaying = PlayerUiState.Loaded(
    song = getLucky,
    status = PlaybackStatus.Playing,
    position = 18.seconds,
    duration = 29.seconds,
    isRepeatEnabled = false,
    hasPrevious = true,
    hasNext = true,
)

internal class PlayerScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun player() {
        capture(
            fileName = "player",
            title = "A player that keeps your queue",
            description = "Scrub, repeat and skip through the list you started playing from",
        ) {
            PlayerContent(uiState = nowPlaying, onEvent = {})
        }
    }
}
