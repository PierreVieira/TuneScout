package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
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
                SongOptionsContent(uiState = SongOptionsUiState(song = song()), onEvent = events::add)
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
                SongOptionsContent(uiState = SongOptionsUiState(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Add to queue").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnAddToQueueClicked)
    }

    @Test
    fun givenASongPlayNextEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = SongOptionsUiState(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Play next").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnPlayNextClicked)
    }

    @Test
    fun givenNoSongYetViewAlbumIsInert() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = SongOptionsUiState(song = null), onEvent = events::add)
            }
        }

        onNodeWithText("View album").performClick()

        assertThat(events).isEmpty()
    }
}
