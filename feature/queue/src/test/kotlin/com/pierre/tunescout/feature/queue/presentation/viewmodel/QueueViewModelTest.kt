package com.pierre.tunescout.feature.queue.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.queueEntries
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.queue.presentation.model.QueueContextTitle
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiAction
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.ui.component.R
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

class QueueViewModelTest {
    private lateinit var viewModel: QueueViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var queueControls: QueueControls
    private lateinit var navigator: Navigator
    private lateinit var actions: MutableList<QueueUiAction>

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
        assertThat(state.contextTitle).isEqualTo(QueueContextTitle.Custom("Random Access Memories"))
    }

    @Test
    fun `GIVEN a playlist is playing WHEN observing THEN exposes its name`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOver(PlaybackContext.Playlist(id = 3, title = "Road trip")))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.contextTitle).isEqualTo(QueueContextTitle.Custom("Road trip"))
    }

    @Test
    fun `GIVEN the liked songs are playing WHEN observing THEN says so`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOver(PlaybackContext.LikedSongs))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.contextTitle).isEqualTo(QueueContextTitle.LikedSongs)
    }

    @Test
    fun `GIVEN the recently played are playing WHEN observing THEN says so`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOver(PlaybackContext.RecentlyPlayed))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.contextTitle).isEqualTo(QueueContextTitle.RecentlyPlayed)
    }

    @Test
    fun `GIVEN a single song is playing WHEN observing THEN there is no title`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playback = queuedOver(PlaybackContext.SingleSong))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.contextTitle).isNull()
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

    @Test
    fun `GIVEN an entry the player cannot reach WHEN tapping it THEN says so instead of jumping to it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = queuedOverAlbum(), playableSongIds = setOf(1))

            // When
            viewModel.onEvent(QueueUiEvent.OnEntryClicked(entryId = "entry-9"))

            // Then
            assertThat(actions).containsExactly(QueueUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { queueControls.skipTo(any()) }
        }

    private fun queuedOverAlbum(): PlaybackState =
        queuedOver(PlaybackContext.Album(id = 10, title = "Random Access Memories"))

    private fun queuedOver(context: PlaybackContext): PlaybackState = playbackState(
        entries = queueEntries(listOf(song(id = 1))) +
            queueEntries(listOf(song(id = 9)), source = QueueSource.UserQueue) +
            queueEntries(listOf(song(id = 2), song(id = 3))),
        currentIndex = 0,
        context = context,
    )

    @Test
    fun `GIVEN queued songs the player cannot reach WHEN observing THEN marks their rows`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playback = queuedOverAlbum(), playableSongIds = setOf(1L, 9L))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.unplayableSongIds).containsExactly(2L, 3L)
        }

    private fun TestScope.prepareScenario(
        playback: PlaybackState,
        playableSongIds: Set<Long>? = null,
    ) {
        playbackStateFlow = MutableStateFlow(playback)
        queueControls = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        actions = mutableListOf()
        val playableSongs = PlayableSongs { song -> playableSongIds?.contains(song.id) ?: true }
        viewModel = QueueViewModel(
            observablePlayback = { playbackStateFlow },
            queueControls = queueControls,
            playableSongs = playableSongs,
            navigator = navigator,
            observablePlayableSongs = { flowOf(playableSongs) },
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
