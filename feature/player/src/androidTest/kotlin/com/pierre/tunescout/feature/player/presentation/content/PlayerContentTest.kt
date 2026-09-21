package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class PlayerContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<PlayerUiEvent>()

    @Test
    fun givenLoadedSongShowsTitleArtistAndTimeline() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(isSideBySide = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Now playing").assertIsDisplayed()
        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithText("Daft Punk").assertIsDisplayed()
        onNodeWithText("0:05").assertIsDisplayed()
        onNodeWithText("-0:25").assertIsDisplayed()
        onNodeWithContentDescription("Playback position")
            .assertIsDisplayed()
            .assert(hasStateDescription("5 seconds of 30 seconds"))
    }

    @Test
    fun givenPlayingSongClickingPauseEmitsPlayPauseEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(
                    isSideBySide = false,
                    uiState = loaded(status = PlaybackStatus.Playing),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Pause").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnPlayPauseClicked)
    }

    @Test
    fun givenPausedSongShowsPlayButton() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(
                    isSideBySide = false,
                    uiState = loaded(status = PlaybackStatus.Paused),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun givenFirstSongOfQueuePreviousIsDisabledAndNextSkips() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(
                    isSideBySide = false,
                    uiState = loaded(hasPrevious = false, hasNext = true),
                    onEvent = events::add,
                )
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
                PlayerContent(isSideBySide = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Back").performClick()
        onNodeWithContentDescription("More options").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnBackClicked, PlayerUiEvent.OnMoreClicked).inOrder()
    }

    @Test
    fun givenAFinishedSongShowsReplayAndClickingItEmitsPlayPause() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(
                    isSideBySide = false,
                    uiState = loaded(status = PlaybackStatus.Ended),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Replay").performClick()

        assertThat(events).containsExactly(PlayerUiEvent.OnPlayPauseClicked)
    }

    @Test
    fun givenADragOnTheTimelineWhenTheSongChangesReleasingSeeksNothing() = compose.use {
        var uiState by mutableStateOf(loaded())
        setContent {
            TuneScoutTheme {
                PlayerContent(isSideBySide = false, uiState = uiState, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Playback position").performTouchInput {
            down(centerLeft)
            moveTo(center)
        }
        uiState = loaded(song = song(id = 2, title = "Instant Crush"), position = Duration.ZERO)
        onNodeWithContentDescription("Playback position").performTouchInput {
            moveTo(centerLeft)
            up()
        }

        assertThat(events).isEmpty()
    }

    @Test
    fun givenShuffleAndRepeatAreOffTheirButtonsSaySoAndEmitTheirEvents() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(isSideBySide = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Shuffle").assertIsOff().performClick()
        onNodeWithContentDescription("Repeat").assert(hasStateDescription("Off")).performClick()

        assertThat(events)
            .containsExactly(PlayerUiEvent.OnShuffleClicked, PlayerUiEvent.OnRepeatClicked)
            .inOrder()
    }

    @Test
    fun givenEachRepeatModeTheButtonNamesIt() = compose.use {
        var repeatMode by mutableStateOf(RepeatMode.All)
        setContent {
            TuneScoutTheme {
                PlayerContent(
                    isSideBySide = false,
                    uiState = loaded(repeatMode = repeatMode, isShuffleEnabled = true),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Shuffle").assertIsOn()
        onNodeWithContentDescription("Repeat").assert(hasStateDescription("Whole queue"))
        repeatMode = RepeatMode.One
        onNodeWithContentDescription("Repeat").assert(hasStateDescription("This song"))
    }

    @Test
    fun givenNotFoundShowsTheMessage() = compose.use {
        setContent {
            TuneScoutTheme {
                PlayerContent(isSideBySide = false, uiState = PlayerUiState.NotFound, onEvent = events::add)
            }
        }

        onNodeWithText("This song is not available").assertIsDisplayed()
    }

    private fun loaded(
        status: PlaybackStatus = PlaybackStatus.Playing,
        hasPrevious: Boolean = true,
        hasNext: Boolean = true,
        song: Song = song(),
        position: Duration = 5.seconds,
        repeatMode: RepeatMode = RepeatMode.Off,
        isShuffleEnabled: Boolean = false,
    ): PlayerUiState.Loaded = PlayerUiState.Loaded(
        song = song,
        status = status,
        position = position,
        duration = 30.seconds,
        repeatMode = repeatMode,
        isShuffleEnabled = isShuffleEnabled,
        hasPrevious = hasPrevious,
        hasNext = hasNext,
    )
}
