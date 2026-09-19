package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class LibraryContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<LibraryUiEvent>()
    private val favorites = LibraryItemUiModel.Favorites(songCount = 2, artworks = emptyList())
    private val roadTrip = LibraryItemUiModel.Playlist(
        id = 7,
        name = "Road trip",
        songCount = 3,
        artworks = emptyList(),
    )

    @Test
    fun theListShowsLikedSongsAndEveryPlaylist() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithText("Your Library").assertIsDisplayed()
        onNodeWithText("Liked songs").assertIsDisplayed()
        onNodeWithText("Road trip").assertIsDisplayed()
        onNodeWithText("Playlist • 3 songs").assertIsDisplayed()
        onNodeWithText("2 songs").assertIsDisplayed()
    }

    @Test
    fun theGridShowsTheSameItems() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(viewMode = LibraryViewMode.GRID), onEvent = events::add)
            }
        }

        onNodeWithText("Liked songs").assertIsDisplayed()
        onNodeWithText("Road trip").assertIsDisplayed()
    }

    @Test
    fun clickingAPlaylistEmitsItsItem() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithText("Road trip").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnItemClicked(roadTrip))
    }

    @Test
    fun theToggleShowsBothModesAndPicksTheOneTapped() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Show as list").assertIsDisplayed()
        onNodeWithContentDescription("Show as grid").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnViewModeSelected(LibraryViewMode.GRID))
    }

    @Test
    fun theHeaderOpensSearchAndPlaylistCreation() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Search your library").performClick()
        onNodeWithContentDescription("Create playlist").performClick()

        assertThat(events)
            .containsExactly(
                LibraryUiEvent.OnSearchClicked,
                LibraryUiEvent.OnCreatePlaylistClicked,
            ).inOrder()
    }

    private fun state(viewMode: LibraryViewMode = LibraryViewMode.LIST): LibraryUiState = LibraryUiState(
        items = listOf(favorites, roadTrip),
        viewMode = viewMode,
    )
}
