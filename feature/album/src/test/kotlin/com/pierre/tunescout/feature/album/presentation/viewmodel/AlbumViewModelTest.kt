package com.pierre.tunescout.feature.album.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.R
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AlbumViewModelTest {
    private lateinit var viewModel: AlbumViewModel
    private lateinit var localAlbum: MutableStateFlow<Album?>
    private lateinit var playbackStarter: PlaybackStarter
    private lateinit var enqueuer: Enqueuer
    private lateinit var navigator: Navigator
    private lateinit var refreshCalls: MutableList<Long>

    /** What the next refresh returns, or null for one that never finishes. */
    private var refreshResults: Result<Unit>? = Result.success(Unit)
    private lateinit var favoriteToggles: MutableList<Pair<Long, Boolean>>
    private lateinit var isOnline: MutableStateFlow<Boolean>
    private lateinit var actions: MutableList<AlbumUiAction>

    @Test
    fun `GIVEN no cached album WHEN starting THEN refreshes it and shows loading meanwhile`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.success(Unit))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Loading)
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a cached album WHEN observing THEN shows it with the playback highlight`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10),
                playback = playbackState(songs = listOf(song(id = 2))),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.album.id).isEqualTo(10L)
            assertThat(state.nowPlaying).isEqualTo(NowPlaying(songId = 2L, isPlaying = true))
        }

    @Test
    fun `GIVEN no cache and a failed refresh WHEN observing THEN shows the error`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Error)
        }

    @Test
    fun `GIVEN a cached album and a failed refresh WHEN observing THEN keeps the cache and marks it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isInstanceOf(AlbumUiState.Loaded::class.java)
            assertThat((state as AlbumUiState.Loaded).isStale).isTrue()
        }

    @Test
    fun `GIVEN a cached album and a successful refresh WHEN observing THEN does not mark it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.isStale).isFalse()
        }

    @Test
    fun `GIVEN a stale album WHEN retrying successfully THEN stops marking it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), refreshResult = Result.failure(IllegalStateException("offline")))
            refreshResults = Result.success(Unit)

            // When
            viewModel.onEvent(AlbumUiEvent.OnRetryClicked)

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isStale).isFalse()
        }

    @Test
    fun `GIVEN only the saved tracks of the album WHEN the refresh is still running THEN shows loading`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10, isComplete = false), refreshResult = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Loading)
        }

    @Test
    fun `GIVEN only the saved tracks of the album and a failed refresh WHEN observing THEN shows them`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val partial = album(id = 10, songs = listOf(song(id = 1)), isComplete = false)
            prepareScenario(cached = partial, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.album).isEqualTo(partial)
        }

    @Test
    fun `GIVEN a partial album WHEN the connection comes back THEN refreshes it and keeps the tracks meanwhile`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10, isComplete = false),
                refreshResult = Result.failure(IllegalStateException("offline")),
                isOnlineAtStart = false,
            )
            refreshResults = null

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(refreshCalls).containsExactly(10L, 10L)
            assertThat(viewModel.uiState.value).isInstanceOf(AlbumUiState.Loaded::class.java)
        }

    @Test
    fun `GIVEN a partial album WHEN the whole album arrives after reconnecting THEN shows the complete one`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10, isComplete = false),
                refreshResult = Result.failure(IllegalStateException("offline")),
                isOnlineAtStart = false,
            )
            refreshResults = Result.success(Unit)

            // When
            isOnline.value = true
            runCurrent()
            localAlbum.value = album(id = 10)

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.album.isComplete).isTrue()
            assertThat(state.isStale).isFalse()
        }

    @Test
    fun `GIVEN a successful refresh WHEN the connection comes back THEN does not ask again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isOnlineAtStart = false)

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a failed refresh WHEN the monitor reports the state the screen opened on THEN does not ask again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Error)
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a partial album WHEN clicking the heart THEN does not store it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            cached = album(id = 10, isComplete = false),
            refreshResult = Result.failure(IllegalStateException("offline")),
        )

        // When
        viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
        runCurrent()

        // Then
        assertThat(favoriteToggles).isEmpty()
    }

    @Test
    fun `GIVEN an error WHEN retrying THEN refreshes again`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

        // When
        viewModel.onEvent(AlbumUiEvent.OnRetryClicked)

        // Then
        assertThat(refreshCalls).containsExactly(10L, 10L)
    }

    @Test
    fun `GIVEN a loaded album WHEN clicking a song THEN plays it with the album as queue and stays on it`() = runTest {
        // Given
        val album = album(id = 10)
        prepareScenario(cached = album)

        // When
        viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

        // Then
        verify {
            playbackStarter.play(
                song = album.songs[1],
                songs = album.songs,
                context = PlaybackContext.Album(id = album.id, title = album.title),
            )
        }
        verify(exactly = 0) { navigator.navigate(any()) }
    }

    @Test
    fun `GIVEN a playing track WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, playback = playbackState(songs = listOf(album.songs[1])))

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = album.songs[1].id)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a paused track WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(songs = listOf(album.songs[1]), status = PlaybackStatus.Paused),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = album.songs[1].id)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a track that ended WHEN clicking its row THEN plays it again from the start`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(songs = listOf(album.songs[1]), status = PlaybackStatus.Ended),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            verify { playbackStarter.play(song = album.songs[1], songs = album.songs, context = any()) }
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN no connection and a track not on the device WHEN clicking it THEN shows a message instead of playing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isOnlineAtStart = false, cachedPreviews = setOf(1L))

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))
            runCurrent()

            // Then
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN no connection and a saved track WHEN clicking it THEN plays it with only the saved tracks queued`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isOnlineAtStart = false, cachedPreviews = setOf(1L))

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[0]))
            runCurrent()

            // Then
            verify {
                playbackStarter.play(
                    song = album.songs[0],
                    songs = listOf(album.songs[0]),
                    context = PlaybackContext.Album(id = album.id, title = album.title),
                )
            }
            assertThat(actions).isEmpty()
        }

    @Test
    fun `GIVEN a connection and a track not on the device WHEN clicking it THEN streams it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, cachedPreviews = emptySet())

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))
            runCurrent()

            // Then
            verify { playbackStarter.play(song = album.songs[1], songs = album.songs, context = any()) }
            assertThat(actions).isEmpty()
        }

    @Test
    fun `GIVEN an album that is not liked WHEN clicking the heart THEN stores it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isFavorite = false)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(10L to false)
        }

    @Test
    fun `GIVEN a liked album WHEN clicking the heart THEN passes the current state through`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isFavorite = true)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(10L to true)
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isFavorite).isTrue()
        }

    @Test
    fun `GIVEN the album has not loaded WHEN clicking the heart THEN does nothing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).isEmpty()
        }

    @Test
    fun `GIVEN a loaded album WHEN playing it now THEN it takes over the current song`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayNowClicked)

            // Then
            verify { enqueuer.playNow(album.songs) }
        }

    @Test
    fun `GIVEN no connection WHEN playing the album now THEN queues only the saved tracks`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isOnlineAtStart = false, cachedPreviews = setOf(2L))

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayNowClicked)

            // Then
            verify { enqueuer.playNow(listOf(album.songs[1])) }
        }

    @Test
    fun `GIVEN no connection and no saved track WHEN playing the album now THEN shows a message instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isOnlineAtStart = false, cachedPreviews = emptySet())

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayNowClicked)
            runCurrent()

            // Then
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { enqueuer.playNow(any()) }
        }

    @Test
    fun `GIVEN the album has not loaded WHEN playing it now THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = null)

        // When
        viewModel.onEvent(AlbumUiEvent.OnPlayNowClicked)

        // Then
        verify(exactly = 0) { enqueuer.playNow(any()) }
    }

    @Test
    fun `GIVEN a loaded album WHEN clicking the overflow THEN opens the album options`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            viewModel.onEvent(AlbumUiEvent.OnMoreClicked)

            // Then
            verify { navigator.navigate(AlbumOptionsRoute(albumId = 10)) }
        }

    @Test
    fun `GIVEN a track WHEN clicking its options THEN opens the same sheet the other lists open`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongOptionsClicked(album.songs.first()))

            // Then
            verify { navigator.navigate(SongOptionsRoute(songId = album.songs.first().id)) }
        }

    @Test
    fun `WHEN clicking back THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = album(id = 10))

        // When
        viewModel.onEvent(AlbumUiEvent.OnBackClicked)

        // Then
        verify { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN a track not on the device WHEN the connection drops THEN marks its row`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), cachedPreviews = setOf(1L))

            // When
            isOnline.value = false
            runCurrent()

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.unplayableSongIds).containsExactly(2L)
        }

    private fun TestScope.prepareScenario(
        cached: Album?,
        refreshResult: Result<Unit>? = Result.success(Unit),
        playback: PlaybackState = PlaybackState.Idle,
        isFavorite: Boolean = false,
        isOnlineAtStart: Boolean = true,
        cachedPreviews: Set<Long> = emptySet(),
    ) {
        localAlbum = MutableStateFlow(cached)
        refreshResults = refreshResult
        refreshCalls = mutableListOf()
        favoriteToggles = mutableListOf()
        isOnline = MutableStateFlow(isOnlineAtStart)
        actions = mutableListOf()
        val playbackStateFlow = MutableStateFlow(playback)
        playbackStarter = mockk(relaxUnitFun = true)
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = AlbumViewModel(
            route = AlbumRoute(albumId = 10),
            useCases = AlbumUseCases(
                observeAlbum = { localAlbum },
                refreshAlbum = { albumId ->
                    refreshCalls += albumId
                    refreshResults ?: awaitCancellation()
                },
                isAlbumFavorite = { flowOf(isFavorite) },
                toggleAlbumFavorite = { album, wasFavorite -> favoriteToggles += album.id to wasFavorite },
                observeIsOnline = { isOnline },
            ),
            observablePlayback = { playbackStateFlow },
            playbackStarter = playbackStarter,
            enqueuer = enqueuer,
            playableSongs = PlayableSongs { song -> isOnline.value || song.id in cachedPreviews },
            navigator = navigator,
            observablePlayableSongs = {
                isOnline.map { isOnline -> PlayableSongs { song -> isOnline || song.id in cachedPreviews } }
            },
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
