package com.pierre.tunescout.feature.album.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AlbumViewModelTest {
    private lateinit var viewModel: AlbumViewModel
    private lateinit var localAlbum: MutableStateFlow<Album?>
    private lateinit var playbackController: PlaybackController
    private lateinit var navigator: Navigator
    private lateinit var refreshCalls: MutableList<Long>

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
                playback = PlaybackState.Idle.copy(currentSong = song(id = 2), status = PlaybackStatus.Playing),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.album.id).isEqualTo(10L)
            assertThat(state.nowPlayingId).isEqualTo(2L)
            assertThat(state.isPlaying).isTrue()
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
    fun `GIVEN a cached album and a failed refresh WHEN observing THEN keeps showing the cache`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isInstanceOf(AlbumUiState.Loaded::class.java)
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
    fun `GIVEN a loaded album WHEN clicking a song THEN plays it with the album as queue and opens the player`() =
        runTest {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            verifyOrder {
                playbackController.play(song = album.songs[1], queue = album.songs)
                navigator.navigate(PlayerRoute(songId = album.songs[1].id))
            }
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

    private fun TestScope.prepareScenario(
        cached: Album?,
        refreshResult: Result<Unit> = Result.success(Unit),
        playback: PlaybackState = PlaybackState.Idle,
    ) {
        localAlbum = MutableStateFlow(cached)
        refreshCalls = mutableListOf()
        playbackController = mockk(relaxUnitFun = true) {
            every { state } returns MutableStateFlow(playback)
        }
        navigator = mockk(relaxUnitFun = true)
        viewModel = AlbumViewModel(
            route = AlbumRoute(albumId = 10),
            observeAlbum = { localAlbum },
            refreshAlbum = { albumId ->
                refreshCalls += albumId
                refreshResult
            },
            playbackController = playbackController,
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
