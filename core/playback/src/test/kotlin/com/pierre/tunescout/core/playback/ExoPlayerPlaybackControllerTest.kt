package com.pierre.tunescout.core.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class ExoPlayerPlaybackControllerTest {
    private val randomAccessMemories = PlaybackContext.Album(id = 10, title = "Random Access Memories")

    private val timeline = mutableListOf<MediaItem>()
    private var currentItemIndex = 0
    private var serviceLaunches = 0
    private val playerListener = slot<Player.Listener>()
    private lateinit var player: ExoPlayer
    private lateinit var controller: ExoPlayerPlaybackController

    @Test
    fun `GIVEN an album WHEN playing a track THEN the timeline is the album and it starts there`() = runTest {
        // Given
        prepareScenario()

        // When
        playAlbum(startingAt = 2)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(
            controller.state.value.currentSong
                ?.id,
        ).isEqualTo(2L)
        assertThat(controller.state.value.context).isEqualTo(randomAccessMemories)
        verify { player.setMediaItems(any(), 1, 0L) }
    }

    @Test
    fun `WHEN adding a song to the queue THEN it lands after the current one and the player agrees`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.addToQueue(listOf(song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 9L, 2L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(timeline.map { item -> item.mediaId })
    }

    @Test
    fun `GIVEN a song is already queued WHEN adding another THEN it goes behind the first`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        controller.addToQueue(listOf(song(id = 9)))

        // When
        controller.addToQueue(listOf(song(id = 8)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 9L, 8L, 2L, 3L).inOrder()
    }

    @Test
    fun `GIVEN a song is already queued WHEN playing another next THEN it jumps ahead of it`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        controller.addToQueue(listOf(song(id = 9)))

        // When
        controller.queueNext(listOf(song(id = 8)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 8L, 9L, 2L, 3L).inOrder()
    }

    @Test
    fun `WHEN queueing songs THEN they are tagged as the user's own`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.addToQueue(listOf(song(id = 9)))

        // Then
        val queued = controller.state.value.entries
            .first { entry -> entry.song.id == 9L }
        assertThat(queued.source).isEqualTo(QueueSource.UserQueue)
    }

    @Test
    fun `GIVEN nothing is playing WHEN adding to the queue THEN playback starts`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.addToQueue(listOf(song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(9L)
        verify { player.prepare() }
        verify { player.play() }
    }

    @Test
    fun `GIVEN a queued song WHEN another album starts THEN the queued song survives it`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        controller.addToQueue(listOf(song(id = 9)))

        // When
        controller.play(
            song = song(id = 5),
            songs = listOf(song(id = 4), song(id = 5), song(id = 6)),
            context = randomAccessMemories,
        )

        // Then
        assertThat(queuedSongIds()).containsExactly(4L, 5L, 9L, 6L).inOrder()
    }

    @Test
    fun `GIVEN a song that already played WHEN another album starts THEN it is not carried over`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        controller.addToQueue(listOf(song(id = 9)))
        currentItemIndex = 1

        // When
        controller.play(song = song(id = 4), songs = listOf(song(id = 4)), context = PlaybackContext.SingleSong)

        // Then
        assertThat(queuedSongIds()).containsExactly(4L)
    }

    @Test
    fun `WHEN removing an entry THEN it leaves both the queue and the player timeline`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        val removed = controller.state.value.entries[1]
            .id

        // When
        controller.removeFromQueue(removed)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L).inOrder()
        assertThat(entryIds()).isEqualTo(timeline.map { item -> item.mediaId })
    }

    @Test
    fun `GIVEN an entry that is not queued WHEN removing it THEN nothing changes`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.removeFromQueue("gone")

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify(exactly = 0) { player.removeMediaItem(any()) }
    }

    @Test
    fun `WHEN moving an entry THEN the queue and the player timeline move together`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.moveInQueue(fromIndex = 2, toIndex = 1)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L, 2L).inOrder()
        assertThat(entryIds()).isEqualTo(timeline.map { item -> item.mediaId })
    }

    @Test
    fun `GIVEN an index outside the queue WHEN moving THEN nothing changes`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.moveInQueue(fromIndex = 0, toIndex = 7)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        verify(exactly = 0) { player.moveMediaItem(any(), any()) }
    }

    @Test
    fun `WHEN skipping to a queued entry THEN the player seeks to it and resumes`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        val target = controller.state.value.entries[2]
            .id

        // When
        controller.skipTo(target)

        // Then
        verify { player.seekTo(2, 0L) }
        assertThat(
            controller.state.value.currentSong
                ?.id,
        ).isEqualTo(3L)
    }

    @Test
    fun `GIVEN a saved session WHEN restoring THEN the player is prepared at its position but not played`() = runTest {
        // Given
        prepareScenario()
        val session = PlaybackSession(
            entries = listOf(
                entry(id = "a", song = song(id = 1)),
                entry(id = "b", song = song(id = 2)),
            ),
            currentEntryId = "b",
            context = randomAccessMemories,
            position = 12.seconds,
            isRepeatEnabled = true,
        )

        // When
        controller.restore(session)

        // Then
        verify { player.setMediaItems(any(), 1, 12_000L) }
        verify { player.prepare() }
        verify(exactly = 0) { player.play() }
        assertThat(
            controller.state.value.currentSong
                ?.id,
        ).isEqualTo(2L)
    }

    @Test
    fun `GIVEN a restored session WHEN pressing play THEN the media service is started`() = runTest {
        // Given
        prepareScenario()
        controller.restore(
            PlaybackSession(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.SingleSong,
                position = 12.seconds,
                isRepeatEnabled = false,
            ),
        )

        // When
        controller.togglePlayPause()

        // Then
        verify { player.play() }
    }

    @Test
    fun `GIVEN the song has finished WHEN pressing play THEN it restarts from the beginning`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        every { player.playbackState } returns Player.STATE_ENDED
        every { player.isPlaying } returns false

        // When
        controller.togglePlayPause()

        // Then
        verify { player.seekTo(0L) }
        verify { player.play() }
    }

    @Test
    fun `GIVEN the song is only paused WHEN pressing play THEN it resumes where it stopped`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        every { player.playbackState } returns Player.STATE_READY
        every { player.isPlaying } returns false

        // When
        controller.togglePlayPause()

        // Then
        verify(exactly = 0) { player.seekTo(any<Long>()) }
        verify { player.play() }
    }

    @Test
    fun `WHEN restoring an empty session THEN the player is left alone`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.restore(
            PlaybackSession(
                entries = emptyList(),
                currentEntryId = null,
                context = null,
                position = 12.seconds,
                isRepeatEnabled = false,
            ),
        )

        // Then
        verify(exactly = 0) { player.setMediaItems(any(), any<Int>(), any<Long>()) }
    }

    private fun entry(
        id: String,
        song: Song,
    ): QueueEntry = QueueEntry(id = id, song = song, source = QueueSource.Context)

    private fun playAlbum(startingAt: Long) {
        val songs = listOf(song(id = 1), song(id = 2), song(id = 3))
        controller.play(
            song = songs.first { song -> song.id == startingAt },
            songs = songs,
            context = randomAccessMemories,
        )
    }

    private fun queuedSongIds(): List<Long> = controller.state.value.entries
        .map { entry -> entry.song.id }

    private fun entryIds(): List<String> = controller.state.value.entries
        .map { entry -> entry.id }

    private fun TestScope.prepareScenario() {
        player = mockk(relaxed = true) {
            every { mediaItemCount } answers { timeline.size }
            every { currentMediaItemIndex } answers { currentItemIndex }
            every { repeatMode } returns Player.REPEAT_MODE_OFF
            every { setMediaItems(any(), any<Int>(), any<Long>()) } answers {
                timeline.clear()
                timeline += firstArg<List<MediaItem>>()
                currentItemIndex = secondArg()
            }
            every { addMediaItems(any<Int>(), any()) } answers {
                timeline.addAll(firstArg(), secondArg<List<MediaItem>>())
            }
            every { removeMediaItem(any()) } answers { timeline.removeAt(firstArg()) }
            every { moveMediaItem(any(), any()) } answers {
                timeline.add(secondArg(), timeline.removeAt(firstArg()))
            }
            every { seekTo(any<Int>(), any<Long>()) } answers { currentItemIndex = firstArg() }
        }
        controller = ExoPlayerPlaybackController(
            player = player,
            serviceLauncher = { serviceLaunches++ },
            mediaItemFactory = { entry -> MediaItem.Builder().setMediaId(entry.id).build() },
            scope = backgroundScope,
        )
        verify { player.addListener(capture(playerListener)) }
    }
}
