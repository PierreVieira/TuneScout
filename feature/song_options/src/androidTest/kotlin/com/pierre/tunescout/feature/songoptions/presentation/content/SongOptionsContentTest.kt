package com.pierre.tunescout.feature.songoptions.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
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
    fun givenASongNotDownloadedDownloadEmitsEvent() = compose.use {
        setContent { SongOptionsContent(uiState = state(song = song(id = 1)), onEvent = events::add) }

        onNodeWithText("Download").performScrollTo().performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnDownloadClicked)
    }

    @Test
    fun givenADownloadedSongTheSheetOffersToRemoveTheDownload() = compose.use {
        setContent {
            SongOptionsContent(uiState = state(song = song(id = 1), isDownloaded = true), onEvent = events::add)
        }

        onNodeWithText("Remove download").performScrollTo().performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnDownloadClicked)
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
    fun givenAListTheUserCanReorderReorderSongsEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song(), isReorderable = true), onEvent = events::add)
            }
        }

        onNodeWithText("Reorder songs").performClick()

        assertThat(events).containsExactly(SongOptionsUiEvent.OnReorderClicked)
    }

    @Test
    fun givenAListTheUserCannotReorderReorderSongsIsNotOffered() = compose.use {
        setContent {
            TuneScoutTheme {
                SongOptionsContent(uiState = state(song = song()), onEvent = events::add)
            }
        }

        onNodeWithText("Reorder songs").assertDoesNotExist()
    }

    private fun state(
        song: Song?,
        isFavorite: Boolean = false,
        isRemovableFromPlaylist: Boolean = false,
        isReorderable: Boolean = false,
        isDownloaded: Boolean = false,
    ): SongOptionsUiState = SongOptionsUiState(
        song = song,
        isFavorite = isFavorite,
        isRemovableFromPlaylist = isRemovableFromPlaylist,
        isReorderable = isReorderable,
        isDownloaded = isDownloaded,
    )
}
