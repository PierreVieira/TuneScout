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
    fun `GIVEN only the position moves WHEN observing THEN emits the state once`() = runTest {
        // Given
        val playing = playbackState(songs = listOf(firstSong), status = PlaybackStatus.Playing)
        prepareScenario(playbackState = playing)

        // When / Then
        useCase().test {
            assertThat(awaitItem().song).isEqualTo(firstSong)
            playbackStates.value = playing.copy(position = 3.seconds)
            playbackStates.value = playing.copy(position = 6.seconds)
            expectNoEvents()
        }
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
