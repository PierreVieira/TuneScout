package com.pierre.tunescout.feature.player.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.seconds

class PlayerViewModelTest {
    private lateinit var viewModel: PlayerViewModel
    private lateinit var playbackState: MutableStateFlow<PlaybackState>
    private lateinit var playbackController: PlaybackController
    private lateinit var navigator: Navigator

    @Test
    fun `GIVEN a cached song and idle playback WHEN observing THEN shows the route song paused at zero`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1, duration = 30.seconds))

            // When
            val state = viewModel.uiState.value as PlayerUiState.Loaded

            // Then
            assertThat(state.song.id).isEqualTo(1L)
            assertThat(state.isPlaying).isFalse()
            assertThat(state.duration).isEqualTo(30.seconds)
            assertThat(state.progress).isEqualTo(0f)
        }

    @Test
    fun `GIVEN the queue advanced to another song WHEN observing THEN shows the playing song instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 1),
                playback = playing(song(id = 2), queue = listOf(song(id = 1), song(id = 2), song(id = 3))),
            )

            // When
            val state = viewModel.uiState.value as PlayerUiState.Loaded

            // Then
            assertThat(state.song.id).isEqualTo(2L)
            assertThat(state.hasPrevious).isTrue()
            assertThat(state.hasNext).isTrue()
        }

    @Test
    fun `GIVEN the shown song is playing WHEN clicking play pause THEN toggles playback`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1), playback = playing(song(id = 1)))

            // When
            viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

            // Then
            verify { playbackController.togglePlayPause() }
        }

    @Test
    fun `GIVEN nothing is playing WHEN clicking play pause THEN starts the shown song alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1))

            // When
            viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

            // Then
            verify { playbackController.play(song = song(id = 1), queue = listOf(song(id = 1))) }
        }

    @Test
    fun `GIVEN a seek finished WHEN handling it THEN seeks without touching play state`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1), playback = playing(song(id = 1)))

            // When
            viewModel.onEvent(PlayerUiEvent.OnSeekFinished(position = 12.seconds))

            // Then
            verify { playbackController.seekTo(position = 12.seconds) }
            verify(exactly = 0) { playbackController.togglePlayPause() }
        }

    @Test
    fun `WHEN clicking more THEN navigates to the options sheet of the shown song`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1))

            // When
            viewModel.onEvent(PlayerUiEvent.OnMoreClicked)

            // Then
            verify { navigator.navigate(SongOptionsRoute(songId = 1)) }
        }

    @Test
    fun `WHEN clicking back THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(routeSong = song(id = 1))

        // When
        viewModel.onEvent(PlayerUiEvent.OnBackClicked)

        // Then
        verify { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN the song is unknown locally and nothing plays WHEN observing THEN reports not found`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(PlayerUiState.NotFound)
        }

    private fun TestScope.prepareScenario(
        routeSong: Song?,
        playback: PlaybackState = PlaybackState.Idle,
    ) {
        playbackState = MutableStateFlow(playback)
        playbackController = mockk(relaxUnitFun = true) {
            every { state } returns playbackState
        }
        navigator = mockk(relaxUnitFun = true)
        viewModel = PlayerViewModel(
            route = PlayerRoute(songId = 1),
            observeSong = { flowOf(routeSong) },
            playbackController = playbackController,
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    private fun playing(
        song: Song,
        queue: List<Song> = listOf(song),
    ): PlaybackState = PlaybackState.Idle.copy(
        currentSong = song,
        queue = queue,
        status = PlaybackStatus.Playing,
        position = 5.seconds,
        duration = 30.seconds,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
