package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.RecentlyPlayedRecorder
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RecentlyPlayedRecorderTest {
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var recentlyPlayed: FakeRecentlyPlayedLocalDataSource

    @Test
    fun `GIVEN playback starts WHEN the song is playing THEN records it once`() = runTest {
        // Given
        prepareScenario()

        // When
        playbackStateFlow.value = playing(song(id = 1))
        runCurrent()
        playbackStateFlow.value = playing(song(id = 1)).copy(status = PlaybackStatus.Paused)
        playbackStateFlow.value = playing(song(id = 1))
        runCurrent()

        // Then
        assertThat(recentlyPlayed.recorded.map { song -> song.id }).containsExactly(1L)
    }

    @Test
    fun `GIVEN a song is only buffering WHEN observing THEN records nothing yet`() = runTest {
        // Given
        prepareScenario()

        // When
        playbackStateFlow.value = playing(song(id = 1)).copy(status = PlaybackStatus.Buffering)
        runCurrent()

        // Then
        assertThat(recentlyPlayed.recorded).isEmpty()
    }

    @Test
    fun `GIVEN the queue advances WHEN the next song plays THEN records it as well`() = runTest {
        // Given
        prepareScenario()

        // When
        playbackStateFlow.value = playing(song(id = 1))
        runCurrent()
        playbackStateFlow.value = playing(song(id = 2))
        runCurrent()

        // Then
        assertThat(recentlyPlayed.recorded.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
    }

    private fun TestScope.prepareScenario() {
        playbackStateFlow = MutableStateFlow(PlaybackState.Idle)
        recentlyPlayed = FakeRecentlyPlayedLocalDataSource()
        RecentlyPlayedRecorder(
            playbackState = playbackStateFlow,
            recentlyPlayedLocalDataSource = recentlyPlayed,
        ).start(backgroundScope)
    }

    private fun playing(song: Song): PlaybackState = playbackState(songs = listOf(song))
}

private class FakeRecentlyPlayedLocalDataSource : RecentlyPlayedLocalDataSource {
    val recorded = mutableListOf<Song>()

    override fun observe(limit: Int): Flow<List<Song>> = error("unused")

    override suspend fun record(song: Song) {
        recorded += song
    }
}
