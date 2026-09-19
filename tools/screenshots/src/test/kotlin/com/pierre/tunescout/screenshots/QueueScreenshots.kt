package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.queue.presentation.content.QueueContent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import org.junit.Test

private val queue = QueueUiState(
    contextTitle = randomAccessMemories.title,
    nowPlaying = contextEntry(getLucky),
    queuedByUser = listOf(userEntry(oneMoreTime), userEntry(aroundTheWorld)),
    upNext = listOf(contextEntry(instantCrush), contextEntry(loseYourselfToDance), contextEntry(touch)),
)

internal class QueueScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun queue() {
        capture(
            fileName = "queue",
            title = "Queue what you want next",
            description = "Songs you add play first, then the album carries on. Drag to reorder, tap to jump",
        ) {
            QueueContent(uiState = queue, onEvent = {})
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
