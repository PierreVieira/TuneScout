package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
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
    fun givenLoadedAlbumTheTopBarQueuesItNextOrAtTheEnd() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = AlbumUiState.Loaded(album = album(), nowPlayingId = null, isPlaying = false),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Play the album next").performClick()
        onNodeWithContentDescription("Add the album to the queue").performClick()

        assertThat(events)
            .containsExactly(AlbumUiEvent.OnPlayNextClicked, AlbumUiEvent.OnAddToQueueClicked)
            .inOrder()
    }

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
                    isHeaderInline = false,
                    uiState = AlbumUiState.Loaded(album = album, nowPlayingId = null, isPlaying = false),
                    onEvent = events::add,
                )
            }
        }

        onAllNodesWithText("Random Access Memories").assertCountEquals(TITLE_IN_TOP_BAR_AND_HEADER)
        onNodeWithText("Give Life Back to Music").assertIsDisplayed()
        onNodeWithText("The Game of Love").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnSongClicked(album.songs[1]))
    }

    @Test
    fun givenErrorShowsRetryThatEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = AlbumUiState.Error, onEvent = events::add)
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
                AlbumContent(isHeaderInline = false, uiState = AlbumUiState.Loading, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Loading").assertIsDisplayed()
        onNodeWithContentDescription("Back").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnBackClicked)
    }

    private companion object {
        const val TITLE_IN_TOP_BAR_AND_HEADER = 2
    }
}
