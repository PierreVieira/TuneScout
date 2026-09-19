package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.NO_QUEUE_INDEX
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.playback.internal.buildEntries
import com.pierre.tunescout.core.playback.internal.buildTimeline
import com.pierre.tunescout.core.playback.internal.getCarriedEntries
import com.pierre.tunescout.core.playback.internal.getUserQueueInsertIndex
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.Test

class QueueTimelineTest {
    private var nextEntryId = 0

    @Test
    fun `GIVEN an album WHEN building the timeline THEN keeps its order and starts at the chosen song`() {
        // Given
        val songs = listOf(song(id = 1), song(id = 2), song(id = 3))

        // When
        val timeline = buildTimeline(
            songs = songs,
            startSongId = 2,
            carriedEntries = emptyList(),
            createEntryId = ::createEntryId,
        )

        // Then
        assertThat(timeline.entries.map { entry -> entry.song.id }).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(timeline.startIndex).isEqualTo(1)
    }

    @Test
    fun `GIVEN songs queued by hand WHEN another context starts THEN they play right after the chosen song`() {
        // Given
        val carried = listOf(
            queueEntry(song = song(id = 9), source = QueueSource.UserQueue),
            queueEntry(song = song(id = 8), source = QueueSource.UserQueue),
        )

        // When
        val timeline = buildTimeline(
            songs = listOf(song(id = 1), song(id = 2), song(id = 3)),
            startSongId = 2,
            carriedEntries = carried,
            createEntryId = ::createEntryId,
        )

        // Then
        assertThat(timeline.entries.map { entry -> entry.song.id }).containsExactly(1L, 2L, 9L, 8L, 3L).inOrder()
        assertThat(timeline.startIndex).isEqualTo(1)
    }

    @Test
    fun `GIVEN a song outside the context WHEN building the timeline THEN starts at the first entry`() {
        // Given
        val songs = listOf(song(id = 1), song(id = 2))

        // When
        val timeline = buildTimeline(
            songs = songs,
            startSongId = 99,
            carriedEntries = emptyList(),
            createEntryId = ::createEntryId,
        )

        // Then
        assertThat(timeline.startIndex).isEqualTo(0)
    }

    @Test
    fun `GIVEN the same song twice WHEN building entries THEN each one gets its own id`() {
        // Given
        val songs = listOf(song(id = 1), song(id = 1))

        // When
        val entries = buildEntries(songs, QueueSource.UserQueue, ::createEntryId)

        // Then
        assertThat(entries.map { entry -> entry.id }).containsNoDuplicates()
        assertThat(entries.map { entry -> entry.source }).containsExactly(QueueSource.UserQueue, QueueSource.UserQueue)
    }

    @Test
    fun `GIVEN played and upcoming entries WHEN getting the carried ones THEN keeps only what is still queued`() {
        // Given
        val entries = listOf(
            queueEntry(song = song(id = 1), source = QueueSource.UserQueue),
            queueEntry(song = song(id = 2)),
            queueEntry(song = song(id = 3), source = QueueSource.UserQueue),
            queueEntry(song = song(id = 4)),
        )

        // When
        val carried = getCarriedEntries(entries = entries, currentIndex = 1)

        // Then
        assertThat(carried.map { entry -> entry.song.id }).containsExactly(3L)
    }

    @Test
    fun `GIVEN songs already queued WHEN getting the insert index THEN lands after the last of them`() {
        // Given
        val entries = listOf(
            queueEntry(song = song(id = 1)),
            queueEntry(song = song(id = 2), source = QueueSource.UserQueue),
            queueEntry(song = song(id = 3), source = QueueSource.UserQueue),
            queueEntry(song = song(id = 4)),
        )

        // When
        val index = getUserQueueInsertIndex(entries = entries, currentIndex = 0)

        // Then
        assertThat(index).isEqualTo(3)
    }

    @Test
    fun `GIVEN nothing queued WHEN getting the insert index THEN lands right after the current song`() {
        // Given
        val entries = listOf(queueEntry(song = song(id = 1)), queueEntry(song = song(id = 2)))

        // When
        val index = getUserQueueInsertIndex(entries = entries, currentIndex = 0)

        // Then
        assertThat(index).isEqualTo(1)
    }

    @Test
    fun `GIVEN nothing is playing WHEN getting the insert index THEN lands at the start`() {
        // When
        val index = getUserQueueInsertIndex(entries = emptyList(), currentIndex = NO_QUEUE_INDEX)

        // Then
        assertThat(index).isEqualTo(0)
    }

    private fun createEntryId(): String = "entry-${nextEntryId++}"
}
