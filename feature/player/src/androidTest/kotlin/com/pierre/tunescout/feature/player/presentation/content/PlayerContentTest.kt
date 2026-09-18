package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class PlayerContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<PlayerUiEvent>()

    @Test
    fun givenLoadedSongShowsTitleArtistAlbumAndTimeline() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithText("Daft Punk").assertIsDisplayed()
        onNodeWithText("Random Access Memories").assertIsDisplayed()
        onNodeWithText("0:05").assertIsDisplayed()
        onNodeWithText("-0:25").assertIsDisplayed()
        onNodeWithContentDescription("Playback position").assertIsDisplayed()
    }

    @Test
    fun givenPlayingSongClickingPauseEmitsPlayPauseEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = loaded(status = PlaybackStatus.Playing), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Pause").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnPlayPauseClicked)
    }

    @Test
    fun givenPausedSongShowsPlayButton() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = loaded(status = PlaybackStatus.Paused), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun givenFirstSongOfQueuePreviousIsDisabledAndNextSkips() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = loaded(hasPrevious = false, hasNext = true), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Previous song").assertIsNotEnabled()
        onNodeWithContentDescription("Next song").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnSkipNextClicked)
    }

    @Test
    fun clickingBackAndMoreEmitTheirEvents() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Back").performClick()
        onNodeWithContentDescription("More options").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnBackClicked, PlayerUiEvent.OnMoreClicked).inOrder()
    }

    @Test
    fun givenNotFoundShowsTheMessage() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(uiState = PlayerUiState.NotFound, onEvent = events::add)
            }
        }

        onNodeWithText("This song is not available").assertIsDisplayed()
    }

    private fun loaded(
        status: PlaybackStatus = PlaybackStatus.Playing,
        hasPrevious: Boolean = true,
        hasNext: Boolean = true,
    ): PlayerUiState.Loaded = PlayerUiState.Loaded(
        song = song(),
        status = status,
        position = 5.seconds,
        duration = 30.seconds,
        isRepeatEnabled = false,
        hasPrevious = hasPrevious,
        hasNext = hasNext,
    )
}
