package com.pierre.tunescout.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.feature.queue.presentation.content.QueueContent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import com.pierre.tunescout.ui.theme.TuneScoutColors
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

internal class QueueScreenshots : ReadmeScreenshotsTest() {
    private val sheetCornerRadius = 28.dp

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
        isPlaying = true,
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
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerContent(isSideBySide = false, uiState = playerBehindSheet, onEvent = {})
                Scrim()
                QueueSheet()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Scrim() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BottomSheetDefaults.ScrimColor),
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BoxScope.QueueSheet() {
        Surface(
            color = TuneScoutColors.sheet,
            shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomSheetDefaults.DragHandle()
                QueueContent(uiState = queue, onEvent = {})
            }
        }
    }
}

private fun contextEntry(song: Song): QueueEntry = QueueEntry(
    id = "context-${song.id}",
    song = song,
    source = QueueSource.Context,
)

private fun userEntry(song: Song): QueueEntry = QueueEntry(
    id = "queued-${song.id}",
    song = song,
    source = QueueSource.UserQueue,
)
