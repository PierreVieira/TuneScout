package com.pierre.tunescout.feature.miniplayer.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiState
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.seconds

class MiniPlayerViewModelTest {
    private lateinit var viewModel: MiniPlayerViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var transportControls: TransportControls
    private lateinit var navigator: Navigator

    @Test
    fun `GIVEN a song is playing WHEN observing THEN exposes it with its progress`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playback = playbackState(
                    songs = listOf(song(id = 1)),
                    position = 15.seconds,
                    duration = 30.seconds,
                ),
            )

            // When
            val state = viewModel.uiState.value as MiniPlayerUiState.Loaded

            // Then
            assertThat(state.song.id).isEqualTo(1L)
            assertThat(state.isPlaying).isTrue()
            assertThat(state.progress).isEqualTo(0.5f)
        }

    @Test
    fun `GIVEN the player has not reported a duration yet WHEN observing THEN falls back to the song`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playback = playbackState(
                    songs = listOf(song(id = 1, duration = 40.seconds)),
                    position = 10.seconds,
                ),
            )

            // When
            val state = viewModel.uiState.value as MiniPlayerUiState.Loaded

            // Then
            assertThat(state.progress).isEqualTo(0.25f)
        }

    @Test
    fun `GIVEN a restored song that is paused WHEN observing THEN still exposes it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playback = playbackState(songs = listOf(song(id = 7)), status = PlaybackStatus.Paused),
            )

            // When
            val state = viewModel.uiState.value as MiniPlayerUiState.Loaded

            // Then
            assertThat(state.song.id).isEqualTo(7L)
            assertThat(state.isPlaying).isFalse()
        }

    @Test
    fun `GIVEN the song has finished WHEN observing THEN reports it as ended`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            playback = playbackState(songs = listOf(song(id = 1)), status = PlaybackStatus.Ended),
        )

        // When
        val state = viewModel.uiState.value as MiniPlayerUiState.Loaded

        // Then
        assertThat(state.hasEnded).isTrue()
        assertThat(state.isPlaying).isFalse()
    }

    @Test
    fun `GIVEN nothing is playing WHEN observing THEN exposes no song`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = PlaybackState.Idle)

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state).isEqualTo(MiniPlayerUiState.Empty)
    }

    @Test
    fun `GIVEN a song is playing WHEN clicking the bar THEN opens the player on it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = playbackState(songs = listOf(song(id = 3))))

            // When
            viewModel.onEvent(MiniPlayerUiEvent.OnClicked)

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 3)) }
        }

    @Test
    fun `GIVEN nothing is playing WHEN clicking the bar THEN does not navigate`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = PlaybackState.Idle)

        // When
        viewModel.onEvent(MiniPlayerUiEvent.OnClicked)

        // Then
        verify(exactly = 0) { navigator.navigate(any()) }
    }

    @Test
    fun `WHEN clicking the queue THEN opens it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = playbackState(songs = listOf(song(id = 1))))

        // When
        viewModel.onEvent(MiniPlayerUiEvent.OnQueueClicked)

        // Then
        verify { navigator.navigate(QueueRoute) }
    }

    @Test
    fun `WHEN clicking play pause THEN toggles playback`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = playbackState(songs = listOf(song(id = 1))))

        // When
        viewModel.onEvent(MiniPlayerUiEvent.OnPlayPauseClicked)

        // Then
        verify { transportControls.togglePlayPause() }
    }

    private fun TestScope.prepareScenario(playback: PlaybackState) {
        playbackStateFlow = MutableStateFlow(playback)
        transportControls = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = MiniPlayerViewModel(
            observablePlayback = { playbackStateFlow },
            transportControls = transportControls,
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
