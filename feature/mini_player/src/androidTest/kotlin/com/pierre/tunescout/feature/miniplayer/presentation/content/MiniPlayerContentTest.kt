package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class MiniPlayerContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<MiniPlayerUiEvent>()
    private val longTitle = "Don't Stop 'Til You Get Enough (Extended Immortal Megamix Version)"

    @Test
    fun givenAPlayingSongShowsItAndPausesOnClick() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    playButtonState = PlayButtonState.Pause,
                    progress = 0.5f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithText("Daft Punk").assertIsDisplayed()
        onNodeWithContentDescription("Pause").performClick()

        assertThat(events).containsExactly(MiniPlayerUiEvent.OnPlayPauseClicked)
    }

    @Test
    fun givenAPausedSongShowsThePlayButton() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    playButtonState = PlayButtonState.Play,
                    progress = 0f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun givenAFinishedSongShowsTheReplayButton() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    playButtonState = PlayButtonState.Replay,
                    progress = 1f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Replay").assertIsDisplayed()
    }

    @Test
    fun clickingTheQueueIconOpensTheQueue() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    playButtonState = PlayButtonState.Pause,
                    progress = 0.5f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Open the queue").performClick()

        assertThat(events).containsExactly(MiniPlayerUiEvent.OnQueueClicked)
    }

    @Test
    fun givenATitleWiderThanTheBarStillShowsItWhole() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(title = longTitle),
                    playButtonState = PlayButtonState.Pause,
                    progress = 0.5f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText(longTitle).assertIsDisplayed()
    }

    @Test
    fun clickingTheBarOpensThePlayer() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    playButtonState = PlayButtonState.Pause,
                    progress = 0.5f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Get Lucky").performClick()

        assertThat(events).containsExactly(MiniPlayerUiEvent.OnClicked)
    }
}
