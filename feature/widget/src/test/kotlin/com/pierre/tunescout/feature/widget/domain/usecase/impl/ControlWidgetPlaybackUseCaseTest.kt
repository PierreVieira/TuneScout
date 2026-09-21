package com.pierre.tunescout.feature.widget.domain.usecase.impl

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.core.utils.DispatcherProvider
import com.pierre.tunescout.feature.widget.FakeRecentlyPlayedLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ControlWidgetPlaybackUseCaseTest {
    private val restoreTimeout = 5.seconds
    private val recentSong = song(id = 7, title = "Recent")

    private lateinit var useCase: ControlWidgetPlaybackUseCase
    private lateinit var playbackStates: MutableStateFlow<PlaybackState>
    private lateinit var transport: RecordingTransportControls
    private lateinit var starter: RecordingPlaybackStarter

    @Test
    fun `GIVEN a restored queue WHEN toggling play THEN reaches the player`() = runTest {
        // Given
        prepareScenario(playbackState = playbackState(songs = listOf(song())))

        // When
        useCase.togglePlayPause()
        advanceUntilIdle()

        // Then
        assertThat(transport.calls).containsExactly("togglePlayPause")
    }

    @Test
    fun `GIVEN a restored queue WHEN skipping THEN reaches the player`() = runTest {
        // Given
        prepareScenario(playbackState = playbackState(songs = listOf(song())))

        // When
        useCase.skipToNext()
        useCase.skipToPrevious()
        advanceUntilIdle()

        // Then
        assertThat(transport.calls).containsExactly("skipToNext", "skipToPrevious").inOrder()
    }

    @Test
    fun `GIVEN a queue still being restored WHEN toggling play THEN waits for it`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle)

        // When
        val tap = launchToggle()
        advanceTimeBy(1.seconds)
        val beforeRestore = transport.calls.toList()
        playbackStates.value = playbackState(songs = listOf(song()))
        tap.join()

        // Then
        assertThat(beforeRestore).isEmpty()
        assertThat(transport.calls).containsExactly("togglePlayPause")
    }

    @Test
    fun `GIVEN a queue that never arrives WHEN toggling play THEN gives up and still forwards it`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle)

        // When
        useCase.togglePlayPause()
        advanceUntilIdle()

        // Then
        assertThat(transport.calls).containsExactly("togglePlayPause")
    }

    @Test
    fun `GIVEN a recent song WHEN playing it THEN starts it with the recent songs as the queue`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle, shortcuts = listOf(recentSong, song(id = 8)))

        // When
        useCase.playSong(recentSong.id)
        advanceUntilIdle()

        // Then
        assertThat(starter.startedSong).isEqualTo(recentSong)
        assertThat(starter.startedQueue).hasSize(2)
        assertThat(starter.startedContext).isEqualTo(PlaybackContext.RecentlyPlayed)
    }

    @Test
    fun `GIVEN a song that is no longer recent WHEN playing it THEN starts nothing`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle, shortcuts = listOf(recentSong))

        // When
        useCase.playSong(songId = 404)
        advanceUntilIdle()

        // Then
        assertThat(starter.startedSong).isNull()
    }

    private fun TestScope.launchToggle(): Job = backgroundScope.launch { useCase.togglePlayPause() }

    private fun TestScope.prepareScenario(
        playbackState: PlaybackState,
        shortcuts: List<Song> = emptyList(),
    ) {
        playbackStates = MutableStateFlow(playbackState)
        transport = RecordingTransportControls()
        starter = RecordingPlaybackStarter()
        useCase = ControlWidgetPlaybackUseCase(
            observablePlayback = ObservablePlayback { playbackStates },
            transportControls = transport,
            playbackStarter = starter,
            recentlyPlayedLocalDataSource = FakeRecentlyPlayedLocalDataSource(songs = shortcuts),
            dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
            restoreTimeout = restoreTimeout,
        )
    }
}

private class RecordingTransportControls : TransportControls {
    val calls: MutableList<String> = mutableListOf()

    override fun togglePlayPause() {
        calls += "togglePlayPause"
    }

    override fun seekTo(position: Duration) {
        error("unused")
    }

    override fun skipToNext() {
        calls += "skipToNext"
    }

    override fun skipToPrevious() {
        calls += "skipToPrevious"
    }

    override fun cycleRepeatMode() {
        error("unused")
    }

    override fun toggleShuffle() {
        error("unused")
    }
}

private class RecordingPlaybackStarter : PlaybackStarter {
    var startedSong: Song? = null
    var startedQueue: List<Song> = emptyList()
    var startedContext: PlaybackContext? = null

    override fun play(
        song: Song,
        songs: List<Song>,
        context: PlaybackContext,
    ) {
        startedSong = song
        startedQueue = songs
        startedContext = context
    }
}

private class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
