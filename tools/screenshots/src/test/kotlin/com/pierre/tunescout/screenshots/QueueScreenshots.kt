package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.feature.queue.presentation.content.QueueContent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.screenshotfixtures.aroundTheWorld
import com.pierre.tunescout.screenshotfixtures.contextEntry
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.instantCrush
import com.pierre.tunescout.screenshotfixtures.loseYourselfToDance
import com.pierre.tunescout.screenshotfixtures.oneMoreTime
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import com.pierre.tunescout.screenshotfixtures.userEntry
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class QueueScreenshots : ReadmeScreenshotsTest() {
    private val playerBehindSheet = PlayerUiState.Loaded(
        song = getLucky,
        status = PlaybackStatus.Playing,
        position = 18.seconds,
        duration = 29.seconds,
        isRepeatEnabled = false,
        hasPrevious = true,
        hasNext = true,
    )
    private val queue = QueueUiState(
        contextTitle = randomAccessMemories.title,
        nowPlaying = contextEntry(getLucky),
        status = PlaybackStatus.Playing,
        queuedByUser = listOf(userEntry(oneMoreTime), userEntry(aroundTheWorld)),
        upNext = listOf(contextEntry(instantCrush), contextEntry(loseYourselfToDance)),
    )

    @Test
    fun queue() {
        capture(
            fileName = "queue",
            title = "Queue what you want next",
            description = "Songs you add play first, then the album carries on. Hold to reorder, tap to jump",
        ) {
            SheetOverScreen(
                screen = { PlayerContent(isSideBySide = false, uiState = playerBehindSheet, onEvent = {}) },
                sheet = { QueueContent(uiState = queue, onEvent = {}) },
            )
        }
    }
}
