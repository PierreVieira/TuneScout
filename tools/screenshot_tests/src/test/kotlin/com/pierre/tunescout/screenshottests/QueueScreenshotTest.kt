package com.pierre.tunescout.screenshottests

import androidx.compose.runtime.Composable
import com.pierre.tunescout.core.model.PlaybackStatus
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

internal class QueueScreenshotTest : ScreenshotTest() {
    private val playing = QueueUiState(
        contextTitle = randomAccessMemories.title,
        nowPlaying = contextEntry(getLucky),
        status = PlaybackStatus.Playing,
        queuedByUser = listOf(userEntry(oneMoreTime), userEntry(aroundTheWorld)),
        upNext = listOf(contextEntry(instantCrush), contextEntry(loseYourselfToDance)),
    )

    @Test
    fun playing() {
        snapshot(name = "playing", variants = ScreenshotVariant.all) {
            QueueSheet(playing)
        }
    }

    @Test
    fun paused() {
        snapshot(name = "paused") {
            QueueSheet(playing.copy(status = PlaybackStatus.Paused))
        }
    }

    /** Nothing queued by hand, so only the context's own songs are up next. */
    @Test
    fun contextOnly() {
        snapshot(name = "context_only") {
            QueueSheet(playing.copy(queuedByUser = emptyList()))
        }
    }

    @Test
    fun empty() {
        snapshot(name = "empty") {
            QueueSheet(
                QueueUiState(
                    contextTitle = null,
                    nowPlaying = null,
                    status = PlaybackStatus.Idle,
                    queuedByUser = emptyList(),
                    upNext = emptyList(),
                ),
            )
        }
    }

    @Composable
    private fun QueueSheet(uiState: QueueUiState) {
        SheetOverScreen(
            screen = {},
            sheet = { QueueContent(uiState = uiState, onEvent = {}) },
        )
    }
}
