package com.pierre.tunescout.feature.player.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiAction
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlayerViewModelTest {
    private lateinit var viewModel: PlayerViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var playbackStarter: PlaybackStarter
    private lateinit var transportControls: TransportControls
    private lateinit var navigator: Navigator
    private lateinit var actions: MutableList<PlayerUiAction>

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
                playback = playing(
                    song = song(id = 2),
                    songs = listOf(song(id = 1), song(id = 2), song(id = 3)),
                ),
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
            verify { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN nothing is playing WHEN clicking play pause THEN starts the shown song alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1))

            // When
            viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

            // Then
            verify {
                playbackStarter.play(
                    song = song(id = 1),
                    songs = listOf(song(id = 1)),
                    context = PlaybackContext.SingleSong,
                )
            }
        }

    @Test
    fun `GIVEN a seek finished WHEN handling it THEN seeks without touching play state`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1), playback = playing(song(id = 1)))

            // When
            viewModel.onEvent(PlayerUiEvent.OnSeekFinished(position = 12.seconds))

            // Then
            verify { transportControls.seekTo(position = 12.seconds) }
            verify(exactly = 0) { transportControls.togglePlayPause() }
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

    @Test
    fun `GIVEN the shown song is one the player cannot reach WHEN clicking play pause THEN says so instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(routeSong = song(id = 1), playableSongIds = emptySet())

            // When
            viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(actions).containsExactly(PlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN a song the player cannot reach is playing WHEN clicking play pause THEN still pauses it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 1),
                playback = playing(song(id = 1)),
                playableSongIds = emptySet(),
            )

            // When
            viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

            // Then
            verify { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN the next song is one the player cannot reach WHEN skipping to it THEN says so instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 1),
                playback = playing(song = song(id = 1), songs = listOf(song(id = 1), song(id = 2))),
                playableSongIds = setOf(1),
            )

            // When
            viewModel.onEvent(PlayerUiEvent.OnSkipNextClicked)

            // Then
            assertThat(actions).containsExactly(PlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { transportControls.skipToNext() }
        }

    @Test
    fun `GIVEN the song before this one cannot be reached WHEN going back THEN says so instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 2),
                playback = playing(
                    song = song(id = 2),
                    songs = listOf(song(id = 1), song(id = 2)),
                    position = Duration.ZERO,
                ),
                playableSongIds = setOf(2),
            )

            // When
            viewModel.onEvent(PlayerUiEvent.OnSkipPreviousClicked)

            // Then
            assertThat(actions).containsExactly(PlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { transportControls.skipToPrevious() }
        }

    @Test
    fun `GIVEN the song is past the window WHEN going back THEN starts it over, whatever came before it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 2),
                playback = playing(
                    song = song(id = 2),
                    songs = listOf(song(id = 1), song(id = 2)),
                    position = PlaybackState.previousSongWindow + 1.seconds,
                ),
                playableSongIds = setOf(2),
            )

            // When
            viewModel.onEvent(PlayerUiEvent.OnSkipPreviousClicked)

            // Then
            assertThat(actions).isEmpty()
            verify { transportControls.skipToPrevious() }
        }

    @Test
    fun `GIVEN the song before this one can be reached WHEN going back THEN goes back to it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                routeSong = song(id = 2),
                playback = playing(
                    song = song(id = 2),
                    songs = listOf(song(id = 1), song(id = 2)),
                    position = Duration.ZERO,
                ),
            )

            // When
            viewModel.onEvent(PlayerUiEvent.OnSkipPreviousClicked)

            // Then
            verify { transportControls.skipToPrevious() }
        }

    private fun TestScope.prepareScenario(
        routeSong: Song?,
        playback: PlaybackState = PlaybackState.Idle,
        playableSongIds: Set<Long>? = null,
    ) {
        playbackStateFlow = MutableStateFlow(playback)
        playbackStarter = mockk(relaxUnitFun = true)
        transportControls = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        actions = mutableListOf()
        viewModel = PlayerViewModel(
            route = PlayerRoute(songId = 1),
            observeSong = { flowOf(routeSong) },
            observablePlayback = { playbackStateFlow },
            playbackStarter = playbackStarter,
            playableSongs = PlayableSongs { song -> playableSongIds?.contains(song.id) ?: true },
            transportControls = transportControls,
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    private fun playing(
        song: Song,
        songs: List<Song> = listOf(song),
        position: Duration = 5.seconds,
    ): PlaybackState = playbackState(
        songs = songs,
        currentIndex = songs.indexOfFirst { queued -> queued.id == song.id },
        position = position,
        duration = 30.seconds,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
