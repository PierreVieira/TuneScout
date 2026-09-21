package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiAction
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.ui.component.R
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.milliseconds

class SongsViewModelTest {
    private lateinit var viewModel: SongsViewModel
    private lateinit var playbackStarter: PlaybackStarter
    private lateinit var navigator: Navigator
    private lateinit var actions: MutableList<SongsUiAction>
    private lateinit var searchedTerms: MutableList<String>
    private lateinit var removedSongIds: MutableList<Long>
    private lateinit var isOnline: MutableStateFlow<Boolean>

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
            assertThat(state.nowPlaying).isEqualTo(NowPlaying(songId = 2L, isPlaying = true))
            assertThat(state.isSearching).isFalse()
        }

    @Test
    fun `GIVEN a song that reached its end WHEN observing THEN marks no song as playing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                recentlyPlayed = listOf(song(id = 2)),
                playback = playbackState(songs = listOf(song(id = 2)), status = PlaybackStatus.Ended),
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.nowPlaying).isNull()
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
            assertThat(results.map { result -> result.song.id }).containsExactly(1L)
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
    fun `WHEN clicking a song THEN plays it alone and stays on the list`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

        // Then
        verify {
            playbackStarter.play(
                song = song(id = 2),
                songs = listOf(song(id = 2)),
                context = PlaybackContext.SingleSong,
            )
        }
        verify(exactly = 0) { navigator.navigate(any()) }
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
    fun `GIVEN a recently played song WHEN swiping it away THEN only asks for confirmation`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(recentlyPlayed = listOf(song(id = 7)))

            // When
            viewModel.onEvent(SongsUiEvent.OnRecentSongSwipedAway(song(id = 7)))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.songPendingRemoval).isEqualTo(song(id = 7))
            assertThat(removedSongIds).isEmpty()
        }

    @Test
    fun `GIVEN a pending removal WHEN confirming it THEN removes the song from the history`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(recentlyPlayed = listOf(song(id = 7)))
            viewModel.onEvent(SongsUiEvent.OnRecentSongSwipedAway(song(id = 7)))

            // When
            viewModel.onEvent(SongsUiEvent.OnRemoveRecentConfirmed)
            runCurrent()

            // Then
            assertThat(removedSongIds).containsExactly(7L)
            assertThat(viewModel.uiState.value.songPendingRemoval).isNull()
        }

    @Test
    fun `GIVEN a pending removal WHEN dismissing it THEN the song stays in the history`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(recentlyPlayed = listOf(song(id = 7)))
            viewModel.onEvent(SongsUiEvent.OnRecentSongSwipedAway(song(id = 7)))

            // When
            viewModel.onEvent(SongsUiEvent.OnRemoveRecentDismissed)
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.songPendingRemoval).isNull()
            assertThat(removedSongIds).isEmpty()
        }

    @Test
    fun `GIVEN no pending removal WHEN confirming THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(recentlyPlayed = listOf(song(id = 7)))

        // When
        viewModel.onEvent(SongsUiEvent.OnRemoveRecentConfirmed)
        runCurrent()

        // Then
        assertThat(removedSongIds).isEmpty()
    }

    @Test
    fun `GIVEN a device with no connection WHEN observing THEN the screen knows it is offline`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(isOnline = false)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.isOffline).isTrue()
        }

    @Test
    fun `GIVEN an offline device WHEN the connection comes back THEN the screen stops saying it is offline`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(isOnline = false)

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.isOffline).isFalse()
        }

    @Test
    fun `GIVEN a search made while offline WHEN the connection comes back THEN searches the term again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(catalog = listOf(song(id = 1)), isOnline = false)
            viewModel.onEvent(SongsUiEvent.OnQueryChanged("daft punk"))
            backgroundScope.launch { viewModel.searchResults.collect {} }
            advanceTimeBy(searchDebounce)
            runCurrent()

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(searchedTerms).containsExactly("daft punk", "daft punk").inOrder()
        }

    @Test
    fun `GIVEN an online device WHEN the connection drops THEN does not search again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(catalog = listOf(song(id = 1)))
            viewModel.onEvent(SongsUiEvent.OnQueryChanged("daft punk"))
            backgroundScope.launch { viewModel.searchResults.collect {} }
            advanceTimeBy(searchDebounce)
            runCurrent()

            // When
            isOnline.value = false
            runCurrent()

            // Then
            assertThat(searchedTerms).containsExactly("daft punk")
        }

    @Test
    fun `GIVEN a song the player cannot reach WHEN clicking it THEN says so instead of playing it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(isOnline = false, playableSongIds = emptySet())

            // When
            viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

            // Then
            assertThat(actions).containsExactly(SongsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN recently played songs the player cannot reach WHEN going offline THEN marks their rows`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                recentlyPlayed = listOf(song(id = 1), song(id = 2)),
                playableSongIds = setOf(1L),
            )

            // When
            isOnline.value = false
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.unplayableSongIds).containsExactly(2L)
        }

    @Test
    fun `GIVEN an offline device WHEN the connection comes back THEN every row is reachable again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                recentlyPlayed = listOf(song(id = 1), song(id = 2)),
                isOnline = false,
                playableSongIds = emptySet(),
            )

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.unplayableSongIds).isEmpty()
        }

    @Test
    fun `GIVEN results the player cannot reach WHEN searching offline THEN marks them`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                catalog = listOf(song(id = 1), song(id = 2)),
                isOnline = false,
                playableSongIds = setOf(1L),
            )

            // When
            viewModel.onEvent(SongsUiEvent.OnQueryChanged("daft punk"))
            val results = viewModel.searchResults.asSnapshot()

            // Then
            assertThat(results.filter { result -> result.isUnavailable }.map { result -> result.song.id })
                .containsExactly(2L)
        }

    @Test
    fun `GIVEN a playing song WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = playbackState(songs = listOf(song(id = 2))))

            // When
            viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 2L)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a paused song WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playback = playbackState(songs = listOf(song(id = 2)), status = PlaybackStatus.Paused),
            )

            // When
            viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 2L)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a playing song WHEN clicking another row THEN plays that one`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = playbackState(songs = listOf(song(id = 2))))

        // When
        viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 3)))

        // Then
        verify {
            playbackStarter.play(
                song = song(id = 3),
                songs = listOf(song(id = 3)),
                context = PlaybackContext.SingleSong,
            )
        }
        verify(exactly = 0) { navigator.navigate(any()) }
    }

    @Test
    fun `GIVEN a song that ended WHEN clicking its row THEN plays it again from the start`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playback = playbackState(songs = listOf(song(id = 2)), status = PlaybackStatus.Ended),
            )

            // When
            viewModel.onEvent(SongsUiEvent.OnSongClicked(song(id = 2)))

            // Then
            verify {
                playbackStarter.play(
                    song = song(id = 2),
                    songs = listOf(song(id = 2)),
                    context = PlaybackContext.SingleSong,
                )
            }
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    private fun TestScope.prepareScenario(
        recentlyPlayed: List<Song> = emptyList(),
        catalog: List<Song> = emptyList(),
        playback: PlaybackState = PlaybackState.Idle,
        isOnline: Boolean = true,
        playableSongIds: Set<Long>? = null,
    ) {
        searchedTerms = mutableListOf()
        actions = mutableListOf()
        removedSongIds = mutableListOf()
        val onlineFlow = MutableStateFlow(isOnline)
        this@SongsViewModelTest.isOnline = onlineFlow
        val playbackStateFlow = MutableStateFlow(playback)
        val reach = { isOnline: Boolean ->
            PlayableSongs { song -> isOnline || playableSongIds?.contains(song.id) ?: true }
        }
        playbackStarter = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = SongsViewModel(
            useCases = SongsUseCases(
                searchSongs = { term ->
                    searchedTerms += term
                    flowOf(PagingData.from(catalog, sourceLoadStates = loadedStates))
                },
                observeRecentlyPlayed = { flowOf(recentlyPlayed) },
                removeFromRecentlyPlayed = { songId -> removedSongIds += songId },
                observeIsOnline = { this@SongsViewModelTest.isOnline },
            ),
            observablePlayback = { playbackStateFlow },
            playbackStarter = playbackStarter,
            playableSongs = PlayableSongs { song -> reach(onlineFlow.value).isPlayable(song) },
            observablePlayableSongs = { onlineFlow.map(reach) },
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    private val searchDebounce = 300.milliseconds
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
