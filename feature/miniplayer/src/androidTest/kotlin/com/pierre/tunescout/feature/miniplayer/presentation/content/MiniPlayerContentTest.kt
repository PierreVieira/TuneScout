package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
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

    @Test
    fun givenAPlayingSongShowsItAndPausesOnClick() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    isPlaying = true,
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
                    isPlaying = false,
                    progress = 0f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun clickingTheBarOpensThePlayer() = compose.use {
        setContent {
            TuneScoutTheme {
                MiniPlayerContent(
                    song = song(),
                    isPlaying = true,
                    progress = 0.5f,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Get Lucky").performClick()

        assertThat(events).containsExactly(MiniPlayerUiEvent.OnClicked)
    }
}
