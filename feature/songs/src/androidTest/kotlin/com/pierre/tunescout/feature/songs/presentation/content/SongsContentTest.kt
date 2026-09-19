package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class SongsContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<SongsUiEvent>()

    @Test
    fun givenNoRecentSongsShowsEmptyMessage() = compose.use {
        setContent { Content(uiState = state(recentlyPlayed = emptyList())) }

        onNodeWithText("Nothing played yet").assertIsDisplayed()
    }

    @Test
    fun givenRecentSongsClickingOneEmitsPlayForThatSongAlone() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"), song(id = 2, title = "Get Lucky"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onNodeWithText("Recently played").assertIsDisplayed()
        onNodeWithText("Get Lucky").performClick()

        assertThat(events).containsExactly(SongsUiEvent.OnSongClicked(recents[1]))
    }

    @Test
    fun givenRecentSongsClickingMoreEmitsOptionsEvent() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onAllNodesWithContentDescription("More options")[0].performClick()

        assertThat(events).containsExactly(SongsUiEvent.OnSongOptionsClicked(recents[0]))
    }

    @Test
    fun givenRecentSongsSwipingOneToTheRightEmitsRemoveForThatSong() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"), song(id = 2, title = "Get Lucky"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onNodeWithText("Get Lucky").performTouchInput { swipeRight() }
        // The row settles on the dismissed anchor first; the callback lands on the next frame.
        waitForIdle()

        assertThat(events).containsExactly(SongsUiEvent.OnRecentSongSwipedAway(recents[1]))
    }

    @Test
    fun givenRecentSongsSwipingOneToTheLeftEmitsRemoveForThatSong() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onNodeWithText("One More Time").performTouchInput { swipeLeft() }
        waitForIdle()

        assertThat(events).containsExactly(SongsUiEvent.OnRecentSongSwipedAway(recents[0]))
    }

    @Test
    fun givenSearchResultsSwipingOneRemovesNothing() = compose.use {
        val results = listOf(song(id = 3, title = "Around the World"))
        setContent { Content(uiState = state(query = "daft"), results = results) }

        onNodeWithText("Around the World").performTouchInput { swipeRight() }
        waitForIdle()

        assertThat(events.filterIsInstance<SongsUiEvent.OnRecentSongSwipedAway>()).isEmpty()
    }

    @Test
    fun typingInTheSearchFieldEmitsQueryChanges() = compose.use {
        setContent { Content(uiState = state(recentlyPlayed = emptyList())) }

        onNode(hasSetTextAction()).performTextInput("daft")

        assertThat(events).contains(SongsUiEvent.OnQueryChanged("daft"))
    }

    @Test
    fun givenAQueryShowsSearchResultsInsteadOfRecents() = compose.use {
        val results = listOf(song(id = 3, title = "Around the World"))
        setContent {
            Content(
                uiState = state(query = "daft", recentlyPlayed = listOf(song(id = 9, title = "Recent"))),
                results = results,
            )
        }

        onNodeWithText("Around the World").assertIsDisplayed()
        onNodeWithText("Recent").assertDoesNotExist()
    }

    @Test
    fun givenAQueryWithoutResultsShowsNoResultsMessage() = compose.use {
        setContent { Content(uiState = state(query = "zzz"), results = emptyList()) }

        onNodeWithText("No songs found").assertIsDisplayed()
    }

    @Composable
    private fun Content(
        uiState: SongsUiState,
        results: List<Song> = emptyList(),
    ) {
        val pagingFlow = remember { flowOf(PagingData.from(results, sourceLoadStates = loadedStates)) }
        TuneScoutTheme {
            SongsContent(
                isHeaderInline = false,
                uiState = uiState,
                searchResults = pagingFlow.collectAsLazyPagingItems(),
                onEvent = events::add,
            )
        }
    }

    private fun state(
        query: String = "",
        recentlyPlayed: List<Song> = emptyList(),
    ): SongsUiState = SongsUiState(query = query, recentlyPlayed = recentlyPlayed, nowPlayingId = null)

    private val loadedStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
}
