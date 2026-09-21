package com.pierre.tunescout.feature.widget.domain.usecase.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.widget.FakeRecentlyPlayedLocalDataSource
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ObserveWidgetStateUseCaseTest {
    private val firstSong = song(id = 1, title = "One")
    private val secondSong = song(id = 2, title = "Two")

    private lateinit var useCase: ObserveWidgetStateUseCase
    private lateinit var playbackStates: MutableStateFlow<PlaybackState>
    private lateinit var recentlyPlayed: FakeRecentlyPlayedLocalDataSource

    @Test
    fun `GIVEN a playing queue WHEN observing THEN maps the song, the status and the skips`() = runTest {
        // Given
        prepareScenario(
            playbackState = playbackState(
                songs = listOf(firstSong, secondSong),
                currentIndex = 0,
                status = PlaybackStatus.Playing,
            ),
            shortcuts = listOf(secondSong),
        )

        // When
        val state = useCase().first()

        // Then
        assertThat(state).isEqualTo(
            WidgetState(
                song = firstSong,
                isPlaying = true,
                hasPrevious = false,
                hasNext = true,
                elapsed = Duration.ZERO,
                total = firstSong.duration,
                shortcuts = listOf(secondSong),
            ),
        )
    }

    @Test
    fun `GIVEN the last song paused WHEN observing THEN keeps the song and reports no next`() = runTest {
        // Given
        prepareScenario(
            playbackState = playbackState(
                songs = listOf(firstSong, secondSong),
                currentIndex = 1,
                status = PlaybackStatus.Paused,
            ),
        )

        // When
        val state = useCase().first()

        // Then
        assertThat(state).isEqualTo(
            WidgetState(
                song = secondSong,
                isPlaying = false,
                hasPrevious = true,
                hasNext = false,
                elapsed = Duration.ZERO,
                total = secondSong.duration,
                shortcuts = emptyList(),
            ),
        )
    }

    @Test
    fun `GIVEN nothing has played WHEN observing THEN reports the empty state`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle)

        // When
        val state = useCase().first()

        // Then
        assertThat(state).isEqualTo(WidgetState.Empty)
    }

    @Test
    fun `GIVEN the position moves within a second WHEN observing THEN emits the state once`() = runTest {
        // Given
        val playing = playbackState(songs = listOf(firstSong), status = PlaybackStatus.Playing)
        prepareScenario(playbackState = playing)

        // When / Then
        useCase().test {
            assertThat(awaitItem().elapsed).isEqualTo(Duration.ZERO)
            playbackStates.value = playing.copy(position = 250.milliseconds)
            playbackStates.value = playing.copy(position = 750.milliseconds)
            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN the position crosses a second WHEN observing THEN emits it rounded down`() = runTest {
        // Given
        val playing = playbackState(songs = listOf(firstSong), status = PlaybackStatus.Playing)
        prepareScenario(playbackState = playing)

        // When / Then
        useCase().test {
            awaitItem()
            playbackStates.value = playing.copy(position = 1_800.milliseconds)
            assertThat(awaitItem().elapsed).isEqualTo(1.seconds)
        }
    }

    @Test
    fun `GIVEN a song the player has not measured WHEN observing THEN falls back to its own length`() = runTest {
        // Given
        prepareScenario(
            playbackState = playbackState(songs = listOf(firstSong), duration = Duration.ZERO),
        )

        // When
        val state = useCase().first()

        // Then
        assertThat(state.total).isEqualTo(firstSong.duration)
        assertThat(state.progress).isEqualTo(0f)
    }

    @Test
    fun `GIVEN a song halfway through WHEN observing THEN reports half the progress`() = runTest {
        // Given
        prepareScenario(
            playbackState = playbackState(
                songs = listOf(firstSong),
                position = 15.seconds,
                duration = 30.seconds,
            ),
        )

        // When
        val state = useCase().first()

        // Then
        assertThat(state.progress).isEqualTo(0.5f)
    }

    @Test
    fun `GIVEN the song changes WHEN observing THEN emits the new state`() = runTest {
        // Given
        val songs = listOf(firstSong, secondSong)
        prepareScenario(playbackState = playbackState(songs = songs, currentIndex = 0))

        // When / Then
        useCase().test {
            awaitItem()
            playbackStates.value = playbackState(songs = songs, currentIndex = 1)
            assertThat(awaitItem().song).isEqualTo(secondSong)
        }
    }

    @Test
    fun `WHEN observing THEN asks for as many recent songs as the widget shows`() = runTest {
        // Given
        prepareScenario(playbackState = PlaybackState.Idle)

        // When
        useCase().first()

        // Then
        assertThat(recentlyPlayed.requestedLimits).containsExactly(WidgetState.SHORTCUT_COUNT)
    }

    private fun prepareScenario(
        playbackState: PlaybackState,
        shortcuts: List<Song> = emptyList(),
    ) {
        playbackStates = MutableStateFlow(playbackState)
        recentlyPlayed = FakeRecentlyPlayedLocalDataSource(songs = shortcuts)
        useCase = ObserveWidgetStateUseCase(
            observablePlayback = ObservablePlayback { playbackStates },
            recentlyPlayedLocalDataSource = recentlyPlayed,
        )
    }
}
