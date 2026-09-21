package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.NO_QUEUE_INDEX
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.playback.internal.QueueTimelineFactory
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.Test
import kotlin.random.Random

class QueueTimelineFactoryTest {
    private var nextEntryId = 0
    private val timelineFactory = QueueTimelineFactory(idGenerator = ::createEntryId, random = Random(seed = 7))

    @Test
    fun `GIVEN an album WHEN building the timeline THEN keeps its order and starts at the chosen song`() {
        // Given
        val songs = listOf(song(id = 1), song(id = 2), song(id = 3))

        // When
        val timeline = timelineFactory.buildTimeline(
            songs = songs,
            startSongId = 2,
            carriedEntries = emptyList(),
            isShuffled = false,
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
        val timeline = timelineFactory.buildTimeline(
            songs = listOf(song(id = 1), song(id = 2), song(id = 3)),
            startSongId = 2,
            carriedEntries = carried,
            isShuffled = false,
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
        val timeline = timelineFactory.buildTimeline(
            songs = songs,
            startSongId = 99,
            carriedEntries = emptyList(),
            isShuffled = false,
        )

        // Then
        assertThat(timeline.startIndex).isEqualTo(0)
    }

    @Test
    fun `GIVEN shuffle WHEN building from the top THEN a random song starts and the rest follows the queued ones`() {
        // Given
        val carried = listOf(queueEntry(song = song(id = 90), source = QueueSource.UserQueue))

        // When
        val timeline = timelineFactory.buildTimeline(
            songs = (1L..8L).map { id -> song(id = id) },
            startSongId = null,
            carriedEntries = carried,
            isShuffled = true,
        )

        // Then
        val songIds = timeline.entries.map { entry -> entry.song.id }
        assertThat(timeline.startIndex).isEqualTo(0)
        assertThat(songIds[1]).isEqualTo(90L)
        assertThat(songIds - 90L).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)
        assertThat(timeline.unshuffledOrder).hasSize(8)
        assertThat(
            timeline.unshuffledOrder.map { id ->
                timeline.entries
                    .first { entry -> entry.id == id }
                    .song.id
            },
        ).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L).inOrder()
    }

    @Test
    fun `GIVEN songs queued by hand ahead WHEN shuffling THEN they lead in their order and the past stays`() {
        // Given
        val entries = listOf(
            queueEntry(id = "c1", song = song(id = 1)),
            queueEntry(id = "c2", song = song(id = 2)),
            queueEntry(id = "c3", song = song(id = 3)),
            queueEntry(id = "q1", song = song(id = 90), source = QueueSource.UserQueue),
            queueEntry(id = "c4", song = song(id = 4)),
            queueEntry(id = "q2", song = song(id = 91), source = QueueSource.UserQueue),
            queueEntry(id = "c5", song = song(id = 5)),
        )

        // When
        val timeline = timelineFactory.buildShuffled(entries, currentIndex = 1)

        // Then
        val ids = timeline.entries.map { entry -> entry.id }
        assertThat(ids.take(4)).containsExactly("c1", "c2", "q1", "q2").inOrder()
        assertThat(ids.drop(4)).containsExactly("c3", "c4", "c5")
        assertThat(timeline.startIndex).isEqualTo(1)
        assertThat(timeline.unshuffledOrder).containsExactly("c1", "c2", "c3", "c4", "c5").inOrder()
    }

    @Test
    fun `GIVEN an empty queue WHEN unshuffling THEN it stays empty`() {
        // When
        val timeline = timelineFactory.buildUnshuffled(
            emptyList(),
            currentIndex = NO_QUEUE_INDEX,
            unshuffledOrder = emptyList(),
        )

        // Then
        assertThat(timeline.entries).isEmpty()
    }

    @Test
    fun `GIVEN a context entry the saved order does not know WHEN unshuffling THEN it goes to the end`() {
        // Given
        val entries = listOf(
            queueEntry(id = "c2", song = song(id = 2)),
            queueEntry(id = "new", song = song(id = 7)),
            queueEntry(id = "c1", song = song(id = 1)),
        )

        // When
        val timeline = timelineFactory.buildUnshuffled(
            entries = entries,
            currentIndex = 0,
            unshuffledOrder = listOf("c1", "c2"),
        )

        // Then
        assertThat(timeline.entries.map { entry -> entry.id }).containsExactly("c1", "c2", "new").inOrder()
        assertThat(timeline.startIndex).isEqualTo(1)
    }

    @Test
    fun `GIVEN the same song twice WHEN building entries THEN each one gets its own id`() {
        // Given
        val songs = listOf(song(id = 1), song(id = 1))

        // When
        val entries = timelineFactory.buildEntries(songs, QueueSource.UserQueue)

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
        val carried = timelineFactory.getCarriedEntries(entries = entries, currentIndex = 1)

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
        val index = timelineFactory.getUserQueueInsertIndex(entries = entries, currentIndex = 0)

        // Then
        assertThat(index).isEqualTo(3)
    }

    @Test
    fun `GIVEN nothing queued WHEN getting the insert index THEN lands right after the current song`() {
        // Given
        val entries = listOf(queueEntry(song = song(id = 1)), queueEntry(song = song(id = 2)))

        // When
        val index = timelineFactory.getUserQueueInsertIndex(entries = entries, currentIndex = 0)

        // Then
        assertThat(index).isEqualTo(1)
    }

    @Test
    fun `GIVEN nothing is playing WHEN getting the insert index THEN lands at the start`() {
        // When
        val index = timelineFactory.getUserQueueInsertIndex(entries = emptyList(), currentIndex = NO_QUEUE_INDEX)

        // Then
        assertThat(index).isEqualTo(0)
    }

    private fun createEntryId(): String = "entry-${nextEntryId++}"
}
