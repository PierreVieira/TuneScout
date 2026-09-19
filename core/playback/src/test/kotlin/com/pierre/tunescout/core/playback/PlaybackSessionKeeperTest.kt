package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.PlaybackSessionLocalDataSource
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.playback.internal.PlaybackSessionKeeper
import com.pierre.tunescout.core.playback.internal.RestorablePlayback
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.queueEntries
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlaybackSessionKeeperTest {
    private val saveInterval = 5.seconds
    private lateinit var playback: FakeRestorablePlayback
    private lateinit var localDataSource: FakePlaybackSessionLocalDataSource

    @Test
    fun `GIVEN a stored session WHEN starting THEN restores it into the player`() = runTest {
        // Given
        val stored = PlaybackSession(
            entries = queueEntries(listOf(song(id = 1), song(id = 2))),
            currentEntryId = "entry-2",
            context = PlaybackContext.Album(id = 10, title = "Random Access Memories"),
            position = 12.seconds,
            isRepeatEnabled = true,
        )

        // When
        prepareScenario(stored = stored)

        // Then
        assertThat(playback.restored).isEqualTo(stored)
    }

    @Test
    fun `GIVEN nothing stored WHEN starting THEN restores nothing`() = runTest {
        // When
        prepareScenario(stored = null)

        // Then
        assertThat(playback.restored).isNull()
    }

    @Test
    fun `WHEN the current song changes THEN saves the session`() = runTest {
        // Given
        prepareScenario(stored = null)
        val songs = listOf(song(id = 1), song(id = 2))

        // When
        playback.state.value = playbackState(songs = songs, currentIndex = 0)
        runCurrent()
        playback.state.value = playbackState(songs = songs, currentIndex = 1)
        runCurrent()

        // Then
        assertThat(localDataSource.saved.map { session -> session.currentEntryId })
            .containsAtLeast("entry-1", "entry-2")
            .inOrder()
    }

    @Test
    fun `GIVEN playback moves within the save interval WHEN observing THEN does not save again`() = runTest {
        // Given
        prepareScenario(stored = null)
        playback.state.value = playing(position = Duration.ZERO)
        runCurrent()
        val savesAfterFirst = localDataSource.saved.size

        // When
        playback.state.value = playing(position = 2.seconds)
        playback.state.value = playing(position = 4.seconds)
        runCurrent()

        // Then
        assertThat(localDataSource.saved).hasSize(savesAfterFirst)
    }

    @Test
    fun `GIVEN playback moves past the save interval WHEN observing THEN saves the new position`() = runTest {
        // Given
        prepareScenario(stored = null)
        playback.state.value = playing(position = Duration.ZERO)
        runCurrent()

        // When
        playback.state.value = playing(position = saveInterval)
        runCurrent()

        // Then
        assertThat(localDataSource.saved.last().position).isEqualTo(saveInterval)
    }

    @Test
    fun `WHEN a song is added to the queue THEN saves the new queue`() = runTest {
        // Given
        prepareScenario(stored = null)
        playback.state.value = playbackState(songs = listOf(song(id = 1)))
        runCurrent()

        // When
        playback.state.value = playbackState(
            entries = queueEntries(listOf(song(id = 1))) +
                queueEntries(listOf(song(id = 9)), source = QueueSource.UserQueue),
        )
        runCurrent()

        // Then
        assertThat(
            localDataSource.saved
                .last()
                .entries
                .map { entry -> entry.song.id },
        ).containsExactly(1L, 9L)
            .inOrder()
    }

    private fun playing(position: Duration): PlaybackState = playbackState(
        songs = listOf(song(id = 1)),
        position = position,
        duration = 30.seconds,
    )

    private fun TestScope.prepareScenario(stored: PlaybackSession?) {
        playback = FakeRestorablePlayback()
        localDataSource = FakePlaybackSessionLocalDataSource(stored = stored)
        PlaybackSessionKeeper(
            playback = playback,
            playbackSessionLocalDataSource = localDataSource,
            saveInterval = saveInterval,
        ).start(backgroundScope)
        runCurrent()
    }
}

private class FakeRestorablePlayback : RestorablePlayback {
    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.Idle)
    var restored: PlaybackSession? = null
        private set

    override fun restore(session: PlaybackSession) {
        restored = session
    }
}

private class FakePlaybackSessionLocalDataSource(
    private val stored: PlaybackSession?,
) : PlaybackSessionLocalDataSource {
    val saved = mutableListOf<PlaybackSession>()

    override suspend fun save(session: PlaybackSession) {
        saved += session
    }

    override suspend fun find(): PlaybackSession? = stored
}
