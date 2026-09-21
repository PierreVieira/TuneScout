package com.pierre.tunescout.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlaybackStateTest {
    @Test
    fun `GIVEN nothing is playing WHEN reading the state THEN there is no song and no way to skip`() {
        // Given
        val state = PlaybackState.Idle

        // When / Then
        assertThat(state.currentEntry).isNull()
        assertThat(state.currentSong).isNull()
        assertThat(state.hasPrevious).isFalse()
        assertThat(state.hasNext).isFalse()
        assertThat(state.upcomingEntries).isEmpty()
    }

    @Test
    fun `GIVEN the first entry is playing WHEN reading the state THEN only next is available`() {
        // Given
        val state = stateOf(currentIndex = 0)

        // When / Then
        assertThat(state.currentSong?.id).isEqualTo(1L)
        assertThat(state.nowPlayingSong?.id).isEqualTo(1L)
        assertThat(state.hasPrevious).isFalse()
        assertThat(state.hasNext).isTrue()
    }

    @Test
    fun `GIVEN the song reached its end WHEN reading the state THEN it stays current, but none is playing`() {
        // Given
        val state = stateOf(currentIndex = 0, status = PlaybackStatus.Ended)

        // When / Then
        assertThat(state.hasEnded).isTrue()
        assertThat(state.isPlaying).isFalse()
        assertThat(state.currentSong?.id).isEqualTo(1L)
        assertThat(state.nowPlayingSong).isNull()
    }

    @Test
    fun `GIVEN the last entry is playing WHEN reading the state THEN only previous is available`() {
        // Given
        val state = stateOf(currentIndex = 2)

        // When / Then
        assertThat(state.hasPrevious).isTrue()
        assertThat(state.hasNext).isFalse()
        assertThat(state.upcomingEntries).isEmpty()
    }

    @Test
    fun `GIVEN an entry in the middle WHEN reading what is upcoming THEN it is everything after it`() {
        // Given
        val state = stateOf(currentIndex = 0)

        // When / Then
        assertThat(state.upcomingEntries.map { entry -> entry.song.id }).containsExactly(2L, 3L).inOrder()
    }

    @Test
    fun `GIVEN a queue WHEN the index is out of range THEN there is no current entry`() {
        // Given
        val state = stateOf(currentIndex = 9)

        // When / Then
        assertThat(state.currentEntry).isNull()
        assertThat(state.hasNext).isFalse()
    }

    @Test
    fun `GIVEN a song that just started WHEN asking what is before it THEN it is the entry behind it`() {
        // Given
        val state = stateOf(currentIndex = 1)

        // When / Then
        assertThat(state.previousEntry?.song?.id).isEqualTo(1L)
    }

    @Test
    fun `GIVEN a song played past the window WHEN asking what is before it THEN there is nothing to go back to`() {
        // Given
        val state = stateOf(currentIndex = 1, position = PlaybackState.previousSongWindow + 1.seconds)

        // When / Then
        assertThat(state.previousEntry).isNull()
    }

    @Test
    fun `GIVEN the first entry is playing WHEN asking what is before it THEN there is nothing to go back to`() {
        // Given
        val state = stateOf(currentIndex = 0)

        // When / Then
        assertThat(state.previousEntry).isNull()
    }

    private fun stateOf(
        currentIndex: Int,
        status: PlaybackStatus = PlaybackStatus.Playing,
        position: Duration = Duration.ZERO,
    ): PlaybackState = PlaybackState.Idle.copy(
        entries = listOf(1L, 2L, 3L).map { id ->
            QueueEntry(id = "entry-$id", song = songOf(id), source = QueueSource.Context)
        },
        currentIndex = currentIndex,
        status = status,
        position = position,
    )

    private fun songOf(id: Long): Song = Song(
        id = id,
        title = "Song $id",
        artistName = "Daft Punk",
        albumId = 10,
        albumTitle = "Discovery",
        artwork = Artwork("https://example.com/$id/100x100bb.jpg"),
        previewUrl = "https://example.com/$id.m4a",
        duration = Duration.ZERO,
        trackNumber = id.toInt(),
    )
}
