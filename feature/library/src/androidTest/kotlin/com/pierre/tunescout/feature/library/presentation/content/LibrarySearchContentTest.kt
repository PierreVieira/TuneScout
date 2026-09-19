package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class LibrarySearchContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<LibrarySearchUiEvent>()
    private val favorites = LibraryItemUiModel.Favorites(songCount = 2, artworks = emptyList())
    private val roadTrip = LibraryItemUiModel.Playlist(
        id = 7,
        name = "Road trip",
        songCount = 3,
        artworks = emptyList(),
    )

    @Test
    fun anEmptyQueryShowsTheRecentSearches() = compose.use {
        setContent {
            TuneScoutTheme {
                LibrarySearchContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithText("Recent searches").assertIsDisplayed()
        onNodeWithText("Road trip").assertIsDisplayed()
    }

    @Test
    fun aRecentSearchCanBeRemovedFromTheList() = compose.use {
        setContent {
            TuneScoutTheme {
                LibrarySearchContent(uiState = state(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Remove from recent searches").performClick()

        assertThat(events).containsExactly(LibrarySearchUiEvent.OnRecentSearchRemoved(roadTrip))
    }

    @Test
    fun aQueryFiltersTheLibraryByName() = compose.use {
        setContent {
            TuneScoutTheme {
                LibrarySearchContent(uiState = state(query = "road"), onEvent = events::add)
            }
        }

        onNodeWithText("Road trip").assertIsDisplayed()
        onNodeWithText("Liked songs").assertDoesNotExist()
    }

    @Test
    fun aQueryThatNamesNothingShowsTheEmptyMessage() = compose.use {
        setContent {
            TuneScoutTheme {
                LibrarySearchContent(uiState = state(query = "podcast"), onEvent = events::add)
            }
        }

        onNodeWithText("Nothing found").assertIsDisplayed()
    }

    @Test
    fun typingEmitsTheQuery() = compose.use {
        setContent {
            TuneScoutTheme {
                LibrarySearchContent(uiState = state(), onEvent = events::add)
            }
        }

        onNode(hasSetTextAction()).performTextInput("road")

        assertThat(events).containsExactly(LibrarySearchUiEvent.OnQueryChanged("road"))
    }

    private fun state(query: String = ""): LibrarySearchUiState = LibrarySearchUiState(
        query = query,
        items = listOf(favorites, roadTrip),
        recentSearches = listOf(roadTrip),
    )
}
