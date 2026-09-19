package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SongsViewModelTest {
    private lateinit var viewModel: SongsViewModel
    private lateinit var playbackController: PlaybackController
    private lateinit var navigator: Navigator
    private lateinit var searchedTerms: MutableList<String>
    private lateinit var removedSongIds: MutableList<Long>

    @Test
    fun `GIVEN recently played songs WHEN observing THEN exposes them with the now playing id`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                recentlyPlayed = listOf(song(id = 1), song(id = 2)),
                playback = playbackState(songs = listOf(song(id = 2))),
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.recentlyPlayed.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
            assertThat(state.nowPlayingId).isEqualTo(2L)
            assertThat(state.isSearching).isFalse()
        }

    @Test
    fun `GIVEN a typed query WHEN the debounce elapses THEN searches the trimmed term`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(catalog = listOf(song(id = 1)))

            // When
            viewModel.onEvent(SongsUiEvent.OnQueryChanged("  daft punk "))
            val results = viewModel.searchResults.asSnapshot()

            // Then
            assertThat(searchedTerms).containsExactly("daft punk")
            assertThat(results.map { song -> song.id }).containsExactly(1L)
            assertThat(viewModel.uiState.value.isSearching).isTrue()
        }

    @Test
    fun `GIVEN a blank query WHEN collecting results THEN searches nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(SongsUiEvent.OnQueryChanged("   "))
        val results = viewModel.searchResults.asSnapshot()

        // Then
        assertThat(searchedTerms).isEmpty()
        assertThat(results).isEmpty()
    }

    @Test
    fun `WHEN clearing the query THEN leaves search mode`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(SongsUiEvent.OnQueryChanged("daft"))

        // When
        viewModel.onEvent(SongsUiEvent.OnClearQueryClicked)

        // Then
        assertThat(viewModel.uiState.value.query).isEmpty()
    }

    @Test
    fun `WHEN clicking a song THEN plays it alone and opens the player`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

        // Then
        verifyOrder {
            playbackController.play(
                song = song(id = 2),
                songs = listOf(song(id = 2)),
                context = PlaybackContext.SingleSong,
            )
            navigator.navigate(PlayerRoute(songId = 2))
        }
    }

    @Test
    fun `WHEN clicking the song options THEN opens the options sheet`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(SongsUiEvent.OnSongOptionsClicked(song = song(id = 3)))

        // Then
        verify { navigator.navigate(SongOptionsRoute(songId = 3)) }
    }

    @Test
    fun `GIVEN a recently played song WHEN swiping it away THEN removes it from the history`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(recentlyPlayed = listOf(song(id = 7)))

            // When
            viewModel.onEvent(SongsUiEvent.OnRecentSongSwipedAway(song(id = 7)))
            runCurrent()

            // Then
            assertThat(removedSongIds).containsExactly(7L)
        }

    private fun TestScope.prepareScenario(
        recentlyPlayed: List<Song> = emptyList(),
        catalog: List<Song> = emptyList(),
        playback: PlaybackState = PlaybackState.Idle,
    ) {
        searchedTerms = mutableListOf()
        removedSongIds = mutableListOf()
        playbackController = mockk(relaxUnitFun = true) {
            every { state } returns MutableStateFlow(playback)
        }
        navigator = mockk(relaxUnitFun = true)
        viewModel = SongsViewModel(
            useCases = SongsUseCases(
                searchSongs = { term ->
                    searchedTerms += term
                    flowOf(PagingData.from(catalog, sourceLoadStates = loadedStates))
                },
                observeRecentlyPlayed = { flowOf(recentlyPlayed) },
                removeFromRecentlyPlayed = { songId -> removedSongIds += songId },
            ),
            playbackController = playbackController,
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    private val loadedStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
