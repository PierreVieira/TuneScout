package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.playback.internal.QueueTimelineFactory
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
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
            timelineFactory = QueueTimelineFactory(idGenerator = ::createEntryId, random = Random(seed = 7)),
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
    fun `GIVEN an album is playing WHEN playing songs now THEN they land next and the player jumps to them`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.playNow(listOf(song(id = 8), song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 8L, 9L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
        verify { fakeExoPlayer.player.seekTo(1, 0L) }
    }

    @Test
    fun `GIVEN nothing is queued WHEN playing songs now THEN they become the whole timeline`() {
        // When
        queue.playNow(listOf(song(id = 8)))

        // Then
        assertThat(queuedSongIds()).containsExactly(8L)
        verify { fakeExoPlayer.player.seekTo(0, 0L) }
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
    fun `WHEN clearing the queue THEN only the currently playing entry survives`() {
        // Given
        startAlbum(startingAt = 1)
        queue.addToQueue(listOf(song(id = 9)))

        // When
        queue.clear()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L)
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN the queue is playing mid-album WHEN clearing THEN everything but what is playing is dropped`() {
        // Given
        startAlbum(startingAt = 2)
        queue.addToQueue(listOf(song(id = 9)))

        // When
        queue.clear()

        // Then
        assertThat(queuedSongIds()).containsExactly(2L)
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN nothing was ever played WHEN clearing THEN nothing changes`() {
        // When
        queue.clear()

        // Then
        assertThat(queue.entries).isEmpty()
        verify(exactly = 0) { fakeExoPlayer.player.removeMediaItems(any(), any()) }
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
        queue.restore(
            restoredEntries = saved,
            currentEntryId = "b",
            position = 12.seconds,
            isShuffled = false,
            unshuffledOrder = listOf("b", "a"),
        )

        // Then
        assertThat(queue.entries).isEqualTo(saved)
        assertThat(queue.isShuffled).isFalse()
        assertThat(queue.unshuffledOrder).isEmpty()
        verify { fakeExoPlayer.player.setMediaItems(any(), 1, 12_000L) }
    }

    @Test
    fun `GIVEN a saved entry that is gone WHEN restoring THEN the timeline starts at the first one`() {
        // Given
        val saved = listOf(queueEntry(id = "a", song = song(id = 1)))

        // When
        queue.restore(
            restoredEntries = saved,
            currentEntryId = "gone",
            position = 12.seconds,
            isShuffled = false,
            unshuffledOrder = emptyList(),
        )

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
    fun `GIVEN songs queued by hand WHEN shuffling THEN only the rest of the album moves and the player agrees`() {
        // Given
        startAlbum(startingAt = 2, songCount = 8)
        queue.addToQueue(listOf(song(id = 90), song(id = 91)))

        // When
        queue.shuffle()

        // Then
        assertThat(queue.isShuffled).isTrue()
        assertThat(queuedSongIds().take(4)).containsExactly(1L, 2L, 90L, 91L).inOrder()
        assertThat(queuedSongIds().drop(4)).containsExactly(3L, 4L, 5L, 6L, 7L, 8L)
        assertThat(queuedSongIds().drop(4)).isNotEqualTo(listOf(3L, 4L, 5L, 6L, 7L, 8L))
        assertThat(queue.currentIndex).isEqualTo(1)
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a shuffled album WHEN adding a song to the queue THEN it still plays before the rest of the album`() {
        // Given
        startAlbum(startingAt = 1, songCount = 8)
        queue.shuffle()

        // When
        queue.addToQueue(listOf(song(id = 90)))

        // Then
        assertThat(queuedSongIds()[1]).isEqualTo(90L)
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a shuffled album WHEN unshuffling THEN it carries on in order from the song playing`() {
        // Given
        startAlbum(startingAt = 1, songCount = 8)
        queue.shuffle()
        queue.addToQueue(listOf(song(id = 90)))
        fakeExoPlayer.currentItemIndex = 4
        val playing = queue.entries[4].song.id

        // When
        queue.unshuffle()

        // Then
        val album = (1L..8L).toList()
        val playingPosition = album.indexOf(playing)
        assertThat(queue.isShuffled).isFalse()
        assertThat(queue.unshuffledOrder).isEmpty()
        assertThat(queuedSongIds()).containsExactlyElementsIn(listOf(90L) + album).inOrder()
        assertThat(queue.currentIndex).isEqualTo(playingPosition + 1)
        assertThat(entryIds()).isEqualTo(fakeExoPlayer.mediaIds)
    }

    @Test
    fun `GIVEN a queued song is playing WHEN unshuffling THEN the album resumes after the last song heard`() {
        // Given
        startAlbum(startingAt = 1, songCount = 5)
        queue.shuffle()
        val lastHeard = queue.entries[1].song.id
        fakeExoPlayer.currentItemIndex = 1
        queue.queueNext(listOf(song(id = 90)))
        fakeExoPlayer.currentItemIndex = 2

        // When
        queue.unshuffle()

        // Then
        val album = (1L..5L).toList()
        val lastHeardPosition = album.indexOf(lastHeard)
        assertThat(queuedSongIds())
            .containsExactlyElementsIn(
                album.take(lastHeardPosition + 1) + 90L + album.drop(lastHeardPosition + 1),
            ).inOrder()
        assertThat(queue.entries[lastHeardPosition + 1].source).isEqualTo(QueueSource.UserQueue)
    }

    @Test
    fun `GIVEN shuffle is on WHEN starting an album at a song THEN it plays first and the rest is shuffled`() {
        // Given
        queue.shuffle()

        // When
        startAlbum(startingAt = 5, songCount = 8)

        // Then
        assertThat(queuedSongIds().first()).isEqualTo(5L)
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)
        assertThat(queue.unshuffledOrder).isEqualTo(entryIds().sortedBy { id -> id.removePrefix("entry-").toInt() })
        verify { fakeExoPlayer.player.setMediaItems(any(), 0, 0L) }
    }

    @Test
    fun `GIVEN shuffle is off WHEN starting an album from the top THEN it starts on its first song`() {
        // When
        queue.startContextFromTop(albumSongs(count = 3))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify { fakeExoPlayer.player.setMediaItems(any(), 0, 0L) }
    }

    @Test
    fun `GIVEN a shuffled queue WHEN shuffling it again THEN nothing moves`() {
        // Given
        startAlbum(startingAt = 1, songCount = 8)
        queue.shuffle()
        val shuffled = queuedSongIds()

        // When
        queue.shuffle()

        // Then
        assertThat(queuedSongIds()).isEqualTo(shuffled)
    }

    @Test
    fun `GIVEN a queue in order WHEN unshuffling it THEN nothing moves`() {
        // Given
        startAlbum(startingAt = 1)

        // When
        queue.unshuffle()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify(exactly = 0) { fakeExoPlayer.player.replaceMediaItems(any(), any(), any()) }
    }

    @Test
    fun `GIVEN a shuffled saved queue WHEN restoring it THEN it stays shuffled with the saved order to put back`() {
        // Given
        val saved = listOf(
            queueEntry(id = "a", song = song(id = 1)),
            queueEntry(id = "c", song = song(id = 3)),
            queueEntry(id = "b", song = song(id = 2)),
        )

        // When
        queue.restore(
            restoredEntries = saved,
            currentEntryId = "a",
            position = 0.seconds,
            isShuffled = true,
            unshuffledOrder = listOf("a", "b", "c"),
        )
        queue.unshuffle()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
    }

    @Test
    fun `GIVEN nothing was ever played WHEN reading the queue THEN it is empty`() {
        // Then
        assertThat(queue.isEmpty).isTrue()
        assertThat(queue.entries).isEmpty()
    }

    private fun startAlbum(
        startingAt: Long,
        songCount: Int = 3,
    ) {
        val songs = albumSongs(count = songCount)
        queue.startContext(song = songs.first { song -> song.id == startingAt }, songs = songs)
    }

    private fun albumSongs(count: Int): List<Song> = (1L..count).map { id -> song(id = id) }

    private fun queuedSongIds(): List<Long> = queue.entries.map { entry -> entry.song.id }

    private fun entryIds(): List<String> = queue.entries.map { entry -> entry.id }

    private fun createEntryId(): String = "entry-${nextEntryId++}"
}
