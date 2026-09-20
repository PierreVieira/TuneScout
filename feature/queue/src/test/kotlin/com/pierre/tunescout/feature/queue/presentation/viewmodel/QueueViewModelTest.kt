package com.pierre.tunescout.feature.queue.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.queueEntries
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class QueueViewModelTest {
    private lateinit var viewModel: QueueViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var queueControls: QueueControls
    private lateinit var navigator: Navigator

    @Test
    fun `GIVEN songs queued by hand WHEN observing THEN they come before the rest of the album`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = queuedOverAlbum())

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.nowPlaying?.song?.id).isEqualTo(1L)
            assertThat(state.isPlaying).isTrue()
            assertThat(state.queuedByUser.map { entry -> entry.song.id }).containsExactly(9L)
            assertThat(state.upNext.map { entry -> entry.song.id }).containsExactly(2L, 3L).inOrder()
        }

    @Test
    fun `GIVEN the song reached its end WHEN observing THEN it is still listed, but as ended`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = playbackState(songs = listOf(song(id = 1)), status = PlaybackStatus.Ended))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.nowPlaying?.song?.id).isEqualTo(1L)
            assertThat(state.isPlaying).isFalse()
            assertThat(state.hasEnded).isTrue()
        }

    @Test
    fun `GIVEN an album is playing WHEN observing THEN exposes its title`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOverAlbum())

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.contextTitle).isEqualTo("Random Access Memories")
    }

    @Test
    fun `GIVEN nothing is playing WHEN observing THEN the queue reads as empty`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = PlaybackState.Idle)

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.isEmpty).isTrue()
    }

    @Test
    fun `WHEN clicking an entry THEN skips to it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOverAlbum())

        // When
        viewModel.onEvent(QueueUiEvent.OnEntryClicked("entry-9"))

        // Then
        verify { queueControls.skipTo("entry-9") }
    }

    @Test
    fun `WHEN clicking the now-playing entry THEN opens the player on it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOverAlbum())

        // When
        viewModel.onEvent(QueueUiEvent.OnNowPlayingClicked)

        // Then
        verify { navigator.navigate(PlayerRoute(songId = 1)) }
    }

    @Test
    fun `GIVEN nothing is playing WHEN clicking the now-playing entry THEN does not navigate`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = PlaybackState.Idle)

            // When
            viewModel.onEvent(QueueUiEvent.OnNowPlayingClicked)

            // Then
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `WHEN removing an entry THEN drops it from the queue`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOverAlbum())

        // When
        viewModel.onEvent(QueueUiEvent.OnRemoveClicked("entry-9"))

        // Then
        verify { queueControls.removeFromQueue("entry-9") }
    }

    @Test
    fun `WHEN dragging an entry onto another THEN moves it to that position`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOverAlbum())

        // When
        viewModel.onEvent(QueueUiEvent.OnEntryMoved(fromEntryId = "entry-3", toEntryId = "entry-9"))

        // Then
        verify { queueControls.moveInQueue(fromIndex = 3, toIndex = 1) }
    }

    @Test
    fun `GIVEN an entry that is no longer queued WHEN moving it THEN does nothing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = queuedOverAlbum())

            // When
            viewModel.onEvent(QueueUiEvent.OnEntryMoved(fromEntryId = "entry-404", toEntryId = "entry-9"))

            // Then
            verify(exactly = 0) { queueControls.moveInQueue(any(), any()) }
        }

    private fun queuedOverAlbum(): PlaybackState = playbackState(
        entries = queueEntries(listOf(song(id = 1))) +
            queueEntries(listOf(song(id = 9)), source = QueueSource.UserQueue) +
            queueEntries(listOf(song(id = 2), song(id = 3))),
        currentIndex = 0,
        context = PlaybackContext.Album(id = 10, title = "Random Access Memories"),
    )

    private fun TestScope.prepareScenario(playback: PlaybackState) {
        playbackStateFlow = MutableStateFlow(playback)
        queueControls = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = QueueViewModel(
            observablePlayback = { playbackStateFlow },
            queueControls = queueControls,
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
