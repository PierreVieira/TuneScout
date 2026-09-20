package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
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
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.ComposeContext
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
    fun givenRecentSongsClickingTheRemoveIconEmitsRemoveForThatSong() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onAllNodesWithContentDescription("Remove from recently played")[0].performClick()

        assertThat(events).containsExactly(SongsUiEvent.OnRecentSongSwipedAway(recents[0]))
    }

    @Test
    fun givenRecentSongsSwipingOneToTheRightEmitsRemoveForThatSong() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"), song(id = 2, title = "Get Lucky"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onNodeWithText("Get Lucky").performTouchInput { swipeRight() }
        waitForTheSwipeCallback()

        assertThat(events).containsExactly(SongsUiEvent.OnRecentSongSwipedAway(recents[1]))
    }

    @Test
    fun givenASwipedRowWaitingOnConfirmationTheRowStaysInPlace() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"))
        val uiState = mutableStateOf(state(recentlyPlayed = recents))
        setContent { Content(uiState = uiState.value) }

        onNodeWithText("One More Time").performTouchInput { swipeRight() }
        waitForTheSwipeCallback()
        uiState.value = state(recentlyPlayed = recents, songPendingRemoval = recents[0])
        waitForIdle()

        onNodeWithText("One More Time").assertIsDisplayed()
        onNodeWithText("One More Time").performClick()
        assertThat(events).contains(SongsUiEvent.OnSongClicked(recents[0]))
    }

    @Test
    fun givenRecentSongsSwipingOneToTheLeftEmitsRemoveForThatSong() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"))
        setContent { Content(uiState = state(recentlyPlayed = recents)) }

        onNodeWithText("One More Time").performTouchInput { swipeLeft() }
        waitForTheSwipeCallback()

        assertThat(events).containsExactly(SongsUiEvent.OnRecentSongSwipedAway(recents[0]))
    }

    @Test
    fun givenSearchResultsSwipingOneRemovesNothing() = compose.use {
        val results = listOf(song(id = 3, title = "Around the World"))
        setContent { Content(uiState = state(query = "daft"), results = results) }

        onNodeWithText("Around the World").performTouchInput { swipeRight() }
        waitForTheSwipeCallback()

        assertThat(events.filterIsInstance<SongsUiEvent.OnRecentSongSwipedAway>()).isEmpty()
    }

    @Test
    fun givenASongIsPlayingItsRowShowsTheAnimatedBars() = compose.use {
        val recents = listOf(song(id = 1, title = "One More Time"), song(id = 2, title = "Get Lucky"))
        setContent {
            Content(
                uiState = state(recentlyPlayed = recents, nowPlaying = NowPlaying(songId = 2, isPlaying = true)),
            )
        }

        onNodeWithContentDescription("Now playing").assertIsDisplayed()
        onNodeWithContentDescription("Paused").assertDoesNotExist()
    }

    @Test
    fun givenPlaybackIsPausedTheRowKeepsTheBarsAtRest() = compose.use {
        val recents = listOf(song(id = 2, title = "Get Lucky"))
        setContent {
            Content(uiState = state(recentlyPlayed = recents, nowPlaying = NowPlaying(songId = 2, isPlaying = false)))
        }

        onNodeWithContentDescription("Paused").assertIsDisplayed()
        onNodeWithContentDescription("Now playing").assertDoesNotExist()
    }

    @Test
    fun givenNoSongIsPlayingNoRowShowsTheBars() = compose.use {
        val recents = listOf(song(id = 2, title = "Get Lucky"))
        setContent { Content(uiState = state(recentlyPlayed = recents, nowPlaying = null)) }

        onNodeWithText("Get Lucky").assertIsDisplayed()
        onNodeWithContentDescription("Now playing").assertDoesNotExist()
        onNodeWithContentDescription("Paused").assertDoesNotExist()
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

    /** The row settles on the dismissed anchor first; the callback lands on the next frame. */
    private fun ComposeContext.waitForTheSwipeCallback() {
        waitForIdle()
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
        nowPlaying: NowPlaying? = null,
        songPendingRemoval: Song? = null,
    ): SongsUiState = SongsUiState(
        query = query,
        recentlyPlayed = recentlyPlayed,
        nowPlaying = nowPlaying,
        songPendingRemoval = songPendingRemoval,
    )

    private val loadedStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
}
