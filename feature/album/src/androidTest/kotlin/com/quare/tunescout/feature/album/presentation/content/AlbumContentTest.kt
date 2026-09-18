package com.quare.tunescout.feature.album.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.quare.tunescout.core.testing.fixture.album
import com.quare.tunescout.core.testing.fixture.song
import com.quare.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.quare.tunescout.feature.album.presentation.model.AlbumUiState
import com.quare.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class AlbumContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<AlbumUiEvent>()

    @Test
    fun givenLoadedAlbumShowsHeaderAndTracks() = compose.use {
        val album = album(
            songs = listOf(
                song(id = 1, title = "Give Life Back to Music"),
                song(id = 2, title = "The Game of Love"),
            ),
        )
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    uiState = AlbumUiState.Loaded(album = album, nowPlayingId = null, isPlaying = false),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Random Access Memories").assertIsDisplayed()
        onNodeWithText("Give Life Back to Music").assertIsDisplayed()
        onNodeWithText("The Game of Love").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnSongClicked(album.songs[1]))
    }

    @Test
    fun givenErrorShowsRetryThatEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(uiState = AlbumUiState.Error, onEvent = events::add)
            }
        }

        onNodeWithText("Couldn't load this album").assertIsDisplayed()
        onNodeWithText("Try again").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnRetryClicked)
    }

    @Test
    fun givenLoadingShowsIndicatorAndBackStillWorks() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(uiState = AlbumUiState.Loading, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Loading").assertIsDisplayed()
        onNodeWithContentDescription("Back").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnBackClicked)
    }
}
