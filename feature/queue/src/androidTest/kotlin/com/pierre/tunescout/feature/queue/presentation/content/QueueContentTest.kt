package com.pierre.tunescout.feature.queue.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class QueueContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<QueueUiEvent>()

    @Test
    fun givenAQueueShowsTheAlbumItPlaysFromAndBothSections() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Queue").assertIsDisplayed()
        onNodeWithText("Playing from Random Access Memories").assertIsDisplayed()
        onNodeWithText("Next in queue").assertIsDisplayed()
        onNodeWithText("Next from Random Access Memories").assertIsDisplayed()
        onNodeWithText("One More Time").assertIsDisplayed()
        onNodeWithText("Around the World").assertIsDisplayed()
    }

    @Test
    fun givenAnEmptyQueueShowsTheEmptyMessage() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(
                    uiState = QueueUiState(
                        contextTitle = null,
                        nowPlaying = null,
                        status = PlaybackStatus.Idle,
                        queuedByUser = emptyList(),
                        upNext = emptyList(),
                    ),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("The queue is empty").assertIsDisplayed()
    }

    @Test
    fun givenTheSongIsPlayingTheNowPlayingRowShowsTheBars() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Now playing").assertIsDisplayed()
    }

    @Test
    fun givenTheSongEndedTheNowPlayingRowStaysButDropsTheBars() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(status = PlaybackStatus.Ended), onEvent = events::add)
            }
        }

        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithContentDescription("Now playing").assertDoesNotExist()
        onNodeWithContentDescription("Paused").assertDoesNotExist()
    }

    @Test
    fun clickingTheNowPlayingRowEmitsOpeningThePlayer() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Get Lucky").performClick()

        assertThat(events).containsExactly(QueueUiEvent.OnNowPlayingClicked)
    }

    @Test
    fun clickingAQueuedSongEmitsSkipToIt() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("One More Time").performClick()

        assertThat(events).containsExactly(QueueUiEvent.OnEntryClicked("entry-9"))
    }

    @Test
    fun clickingRemoveOnAQueuedSongEmitsRemoveForThatEntry() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onAllNodesWithContentDescription("Remove from the queue")[0].performClick()

        assertThat(events).containsExactly(QueueUiEvent.OnRemoveClicked("entry-9"))
    }

    @Test
    fun movingAQueuedSongDownEmitsAMoveOntoTheNextEntry() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("One More Time").performCustomAccessibilityActionWithLabel("Move down")

        assertThat(events).containsExactly(
            QueueUiEvent.OnEntryMoved(fromEntryId = "entry-9", toEntryId = "entry-2"),
        )
    }

    @Test
    fun movingTheFirstUpNextSongUpPutsItAheadOfTheQueuedOne() = compose.use {
        setContent {
            TuneScoutTheme {
                QueueContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Around the World").performCustomAccessibilityActionWithLabel("Move up")

        assertThat(events).containsExactly(
            QueueUiEvent.OnEntryMoved(fromEntryId = "entry-2", toEntryId = "entry-9"),
        )
    }

    private fun loaded(status: PlaybackStatus = PlaybackStatus.Playing): QueueUiState = QueueUiState(
        contextTitle = "Random Access Memories",
        nowPlaying = queueEntry(song = song(id = 1, title = "Get Lucky")),
        status = status,
        queuedByUser = listOf(userEntry(id = 9, title = "One More Time")),
        upNext = listOf(queueEntry(song = song(id = 2, title = "Around the World"))),
    )

    private fun userEntry(
        id: Long,
        title: String,
    ): QueueEntry = queueEntry(song = song(id = id, title = title), source = QueueSource.UserQueue)
}
