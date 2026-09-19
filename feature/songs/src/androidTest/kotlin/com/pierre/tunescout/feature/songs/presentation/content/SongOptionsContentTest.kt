package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class SongOptionsContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<SongOptionsUiEvent>()

    @Test
    fun givenASongShowsItsNamesAndViewAlbumEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithText("Daft Punk").assertIsDisplayed()
        onNodeWithText("View album").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnViewAlbumClicked)
    }

    @Test
    fun givenASongAddToQueueEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Add to queue").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnAddToQueueClicked)
    }

    @Test
    fun givenASongPlayNextEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Play next").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnPlayNextClicked)
    }

    @Test
    fun givenNoSongYetViewAlbumIsInert() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = null), onEvent = events::add)
            }
        }

        onNodeWithText("View album").performClick()

        assertThat(events).isEmpty()
    }

    @Test
    fun givenARecentlyPlayedSongRemoveEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(
                    uiState = state(song = song(), isRecentlyPlayed = true),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Remove from recently played").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked)
    }

    @Test
    fun givenASongOutsideTheHistoryRemoveIsHidden() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Remove from recently played").assertDoesNotExist()
    }

    private fun state(
        song: Song?,
        isRecentlyPlayed: Boolean = false,
    ): SongOptionsUiState = SongOptionsUiState(song = song, isRecentlyPlayed = isRecentlyPlayed)
}
