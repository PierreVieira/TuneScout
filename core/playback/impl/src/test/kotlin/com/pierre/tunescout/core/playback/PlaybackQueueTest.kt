package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

internal class PlaybackQueueTest {
    private val fakeExoPlayer = FakeExoPlayer()
    private var nextEntryId = 0
    private lateinit var queue: PlaybackQueue

    @BeforeEach
    fun setUp() {
        queue = PlaybackQueue(
            player = fakeExoPlayer.player,
            mediaItemFactory = ::createTestMediaItem,
            idGenerator = ::createEntryId,
        )
    }

    @Test
    fun `GIVEN an album WHEN starting its context THEN the timeline is the album and it starts at the chosen song`() {
        // When
        startAlbum(startingAt = 2)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
        verify { fakeExoPlayer.player.setMediaItems(any(), 1, 0L) }
    }

    @Test
    fun `GIVEN a song outside its own context WHEN starting it THEN it becomes the whole timeline`() {
        // When
        queue.startContext(song = song(id = 7), songs = emptyList())

        // Then
        assertThat(queuedSongIds()).containsExactly(7L)
    }

    @Test
    fun `WHEN adding a song to the queue THEN it lands after the current one and the player agrees`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.addToQueue(listOf(song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 9L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a song is already queued WHEN adding another THEN it goes behind the first`() {
        // Given
        startAlbum(startingAt = 1)
        queue.addToQueue(listOf(song(id = 9)))

        // When
        queue.addToQueue(listOf(song(id = 8)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 9L, 8L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a song is already queued WHEN playing another next THEN it jumps ahead of it`() {
        // Given
        startAlbum(startingAt = 1)
        queue.addToQueue(listOf(song(id = 9)))

        // When
        queue.queueNext(listOf(song(id = 8)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 8L, 9L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `WHEN queueing songs THEN they are tagged as the user's own`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.addToQueue(listOf(song(id = 9)))

        // Then
        val queued = queue.entries.first { entry -> entry.song.id == 9L }
        assertThat(queued.source).isEqualTo(QueueSource.UserQueue)
    }

    @Test
    fun `GIVEN a queued song WHEN another context starts THEN the queued song survives it`() {
        // Given
        startAlbum(startingAt = 1)
        queue.addToQueue(listOf(song(id = 9)))

        // When
        queue.startContext(
            song = song(id = 5),
            songs = listOf(song(id = 4), song(id = 5), song(id = 6)),
        )

        // Then
        assertThat(queuedSongIds()).containsExactly(4L, 5L, 9L, 6L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a song that already played WHEN another context starts THEN it is not carried over`() {
        // Given
        startAlbum(startingAt = 1)
        queue.addToQueue(listOf(song(id = 9)))
        fakeExoPlayer.currentItemIndex = 1

        // When
        queue.startContext(song = song(id = 4), songs = listOf(song(id = 4)))

        // Then
        assertThat(queuedSongIds()).containsExactly(4L)
    }

    @Test
    fun `WHEN removing an entry THEN it leaves both the queue and the player timeline`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.remove(queue.entries[1].id)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN an entry that is not queued WHEN removing it THEN nothing changes`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.remove("gone")

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify(exactly = 0) { fakeExoPlayer.player.removeMediaItem(any()) }
    }

    @Test
    fun `WHEN moving an entry THEN the queue and the player timeline move together`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.move(fromIndex = 2, toIndex = 1)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L, 2L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN an index outside the queue WHEN moving THEN nothing changes`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.move(fromIndex = 0, toIndex = 7)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify(exactly = 0) { fakeExoPlayer.player.moveMediaItem(any(), any()) }
    }

    @Test
    fun `GIVEN a saved queue WHEN restoring it THEN the timeline starts at the saved entry`() {
        // Given
        val saved = listOf(
            queueEntry(id = "a", song = song(id = 1)),
            queueEntry(id = "b", song = song(id = 2)),
        )

        // When
        queue.restore(restoredEntries = saved, currentEntryId = "b", position = 12.seconds)

        // Then
        assertThat(queue.entries).isEqualTo(saved)
        verify { fakeExoPlayer.player.setMediaItems(any(), 1, 12_000L) }
    }

    @Test
    fun `GIVEN a saved entry that is gone WHEN restoring THEN the timeline starts at the first one`() {
        // Given
        val saved = listOf(queueEntry(id = "a", song = song(id = 1)))

        // When
        queue.restore(restoredEntries = saved, currentEntryId = "gone", position = 12.seconds)

        // Then
        verify { fakeExoPlayer.player.setMediaItems(any(), 0, 12_000L) }
    }

    @Test
    fun `GIVEN an entry that is not queued WHEN looking for its index THEN it is not found`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        val index = queue.findIndex("gone")

        // Then
        assertThat(index).isLessThan(0)
    }

    @Test
    fun `GIVEN nothing was ever played WHEN reading the queue THEN it is empty`() {
        // Then
        assertThat(queue.isEmpty).isTrue()
        assertThat(queue.entries).isEmpty()
    }

    private fun startAlbum(startingAt: Long) {
        val songs = listOf(song(id = 1), song(id = 2), song(id = 3))
        queue.startContext(song = songs.first { song -> song.id == startingAt }, songs = songs)
    }

    private fun queuedSongIds(): List<Long> = queue.entries.map { entry -> entry.song.id }

    private fun entryIds(): List<String> = queue.entries.map { entry -> entry.id }

    private fun createEntryId(): String = "entry-${nextEntryId++}"
}
