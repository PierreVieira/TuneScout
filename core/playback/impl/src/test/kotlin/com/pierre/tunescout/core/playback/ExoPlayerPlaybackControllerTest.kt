package com.pierre.tunescout.core.playback

import androidx.media3.common.Player
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.playback.internal.QueueTimelineFactory
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.core.utils.UuidIdGenerator
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ExoPlayerPlaybackControllerTest {
    private val randomAccessMemories = PlaybackContext.Album(id = 10, title = "Random Access Memories")
    private val roadTrip = PlaybackContext.Playlist(id = 3, title = "Road trip")

    private val fakeExoPlayer = FakeExoPlayer()
    private var serviceLaunches = 0
    private val playerListener = slot<Player.Listener>()
    private lateinit var controller: ExoPlayerPlaybackController

    private val currentState: PlaybackState
        get() = controller.observePlaybackState().value

    @Test
    fun `GIVEN an album WHEN playing a track THEN the queue is published with its context and starts`() = runTest {
        // Given
        prepareScenario()

        // When
        playAlbum(startingAt = 2)

        // Then
        assertThat(
            currentState.currentSong
                ?.id,
        ).isEqualTo(2L)
        assertThat(currentState.context).isEqualTo(randomAccessMemories)
        verify { fakeExoPlayer.player.prepare() }
        verify { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN nothing is playing WHEN adding to the queue THEN playback starts`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.addToQueue(listOf(song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(9L)
        verify { fakeExoPlayer.player.prepare() }
        verify { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN an album is already playing WHEN adding to the queue THEN playback is not restarted`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.addToQueue(listOf(song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 9L, 2L, 3L).inOrder()
        verify(exactly = 1) { fakeExoPlayer.player.prepare() }
    }

    @Test
    fun `GIVEN an album is playing WHEN playing a list now THEN it takes over without losing the queue`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.playNow(listOf(song(id = 8), song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 8L, 9L, 2L, 3L).inOrder()
        assertThat(
            currentState.currentSong
                ?.id,
        ).isEqualTo(8L)
        verify(exactly = 2) { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN nothing is playing WHEN playing a list now THEN playback starts on its first song`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.playNow(listOf(song(id = 8), song(id = 9)))

        // Then
        assertThat(queuedSongIds()).containsExactly(8L, 9L).inOrder()
        verify { fakeExoPlayer.player.prepare() }
        verify { fakeExoPlayer.player.play() }
    }

    @Test
    fun `WHEN playing an empty list now THEN the player is left alone`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.playNow(emptyList())

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.play() }
    }

    @Test
    fun `WHEN removing an entry THEN the shortened queue is published`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        val removed = currentState.entries[1]
            .id

        // When
        controller.removeFromQueue(removed)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `WHEN clearing the queue THEN only the entry playing survives and it is published`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.clearQueue()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L)
    }

    @Test
    fun `WHEN moving an entry THEN the reordered queue is published`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.moveInQueue(fromIndex = 2, toIndex = 1)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 3L, 2L).inOrder()
    }

    @Test
    fun `WHEN skipping to a queued entry THEN the player seeks to it and resumes`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        val target = currentState.entries[2]
            .id

        // When
        controller.skipTo(target)

        // Then
        verify { fakeExoPlayer.player.seekTo(2, 0L) }
        assertThat(
            currentState.currentSong
                ?.id,
        ).isEqualTo(3L)
    }

    @Test
    fun `GIVEN an entry that is not queued WHEN skipping to it THEN the player is left alone`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)

        // When
        controller.skipTo("gone")

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.seekTo(any<Int>(), any<Long>()) }
    }

    @Test
    fun `GIVEN a saved session WHEN restoring THEN the player is prepared at its position but not played`() = runTest {
        // Given
        prepareScenario()
        val session = PlaybackSession(
            entries = listOf(
                queueEntry(id = "a", song = song(id = 1)),
                queueEntry(id = "b", song = song(id = 2)),
            ),
            currentEntryId = "b",
            context = randomAccessMemories,
            position = 12.seconds,
            repeatMode = RepeatMode.One,
            isShuffleEnabled = false,
            unshuffledOrder = emptyList(),
            hasEnded = false,
        )

        // When
        controller.restore(session)

        // Then
        verify { fakeExoPlayer.player.setMediaItems(any(), 1, 12_000L) }
        verify { fakeExoPlayer.player.prepare() }
        verify(exactly = 0) { fakeExoPlayer.player.play() }
        assertThat(
            currentState.currentSong
                ?.id,
        ).isEqualTo(2L)
    }

    @Test
    fun `GIVEN a restored session WHEN playback starts THEN the media service is started`() = runTest {
        // Given
        prepareScenario()
        controller.restore(
            PlaybackSession(
                entries = listOf(queueEntry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.SingleSong,
                position = 12.seconds,
                repeatMode = RepeatMode.Off,
                isShuffleEnabled = false,
                unshuffledOrder = emptyList(),
                hasEnded = false,
            ),
        )

        // When
        controller.togglePlayPause()
        playerListener.captured.onIsPlayingChanged(true)

        // Then
        verify { fakeExoPlayer.player.play() }
        assertThat(serviceLaunches).isEqualTo(1)
    }

    @Test
    fun `GIVEN the saved song had ended WHEN restoring THEN it is still ended`() = runTest {
        // Given
        prepareScenario()
        every { fakeExoPlayer.player.playbackState } returns Player.STATE_READY

        // When
        controller.restore(endedSession())

        // Then
        assertThat(currentState.hasEnded).isTrue()
        assertThat(currentState.nowPlayingSong).isNull()
    }

    @Test
    fun `GIVEN a restored ended song WHEN pressing play THEN it restarts from the beginning`() = runTest {
        // Given
        prepareScenario()
        every { fakeExoPlayer.player.playbackState } returns Player.STATE_READY
        controller.restore(endedSession())

        // When
        controller.togglePlayPause()

        // Then
        verify { fakeExoPlayer.player.seekTo(0L) }
        verify { fakeExoPlayer.player.play() }
        assertThat(currentState.hasEnded).isFalse()
    }

    @Test
    fun `GIVEN a restored ended song WHEN seeking THEN it is no longer ended`() = runTest {
        // Given
        prepareScenario()
        every { fakeExoPlayer.player.playbackState } returns Player.STATE_READY
        controller.restore(endedSession())

        // When
        controller.seekTo(5.seconds)

        // Then
        assertThat(currentState.hasEnded).isFalse()
    }

    @Test
    fun `GIVEN a restored ended song WHEN the player fails THEN the failure is published`() = runTest {
        // Given
        prepareScenario()
        controller.restore(endedSession())
        every { fakeExoPlayer.player.playerError } returns mockk()

        // When
        playerListener.captured.onPlayerErrorChanged(fakeExoPlayer.player.playerError)

        // Then
        assertThat(currentState.status).isEqualTo(PlaybackStatus.Failed)
    }

    @Test
    fun `GIVEN the song has finished WHEN pressing play THEN it restarts from the beginning`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        every { fakeExoPlayer.player.playbackState } returns Player.STATE_ENDED
        every { fakeExoPlayer.player.isPlaying } returns false

        // When
        controller.togglePlayPause()

        // Then
        verify { fakeExoPlayer.player.seekTo(0L) }
        verify { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN the song is only paused WHEN pressing play THEN it resumes where it stopped`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        every { fakeExoPlayer.player.playbackState } returns Player.STATE_READY
        every { fakeExoPlayer.player.isPlaying } returns false

        // When
        controller.togglePlayPause()

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.seekTo(any<Long>()) }
        verify { fakeExoPlayer.player.play() }
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
                repeatMode = RepeatMode.Off,
                isShuffleEnabled = false,
                unshuffledOrder = emptyList(),
                hasEnded = false,
            ),
        )

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.setMediaItems(any(), any<Int>(), any<Long>()) }
    }

    @Test
    fun `GIVEN a saved session WHEN restoring THEN its repeat mode is the player's`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.restore(
            PlaybackSession(
                entries = listOf(queueEntry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.SingleSong,
                position = 0.seconds,
                repeatMode = RepeatMode.All,
                isShuffleEnabled = false,
                unshuffledOrder = emptyList(),
                hasEnded = false,
            ),
        )

        // Then
        assertThat(fakeExoPlayer.repeatMode).isEqualTo(Player.REPEAT_MODE_ALL)
        assertThat(currentState.repeatMode).isEqualTo(RepeatMode.All)
    }

    @Test
    fun `GIVEN a shuffled session WHEN restoring THEN it keeps the saved order and can still put it back`() = runTest {
        // Given
        prepareScenario()
        val saved = listOf(
            queueEntry(id = "a", song = song(id = 1)),
            queueEntry(id = "c", song = song(id = 3)),
            queueEntry(id = "b", song = song(id = 2)),
        )

        // When
        controller.restore(
            PlaybackSession(
                entries = saved,
                currentEntryId = "a",
                context = randomAccessMemories,
                position = 0.seconds,
                repeatMode = RepeatMode.Off,
                isShuffleEnabled = true,
                unshuffledOrder = listOf("a", "b", "c"),
                hasEnded = false,
            ),
        )
        playerListener.captured.onShuffleModeEnabledChanged(true)

        // Then
        assertThat(fakeExoPlayer.shuffleModeEnabled).isTrue()
        assertThat(queuedSongIds()).containsExactly(1L, 3L, 2L).inOrder()
        assertThat(currentState.isShuffleEnabled).isTrue()
        assertThat(currentState.unshuffledOrder).containsExactly("a", "b", "c").inOrder()

        // When
        controller.toggleShuffle()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
    }

    @Test
    fun `WHEN cycling the repeat mode THEN it goes from off to the whole queue to the song and back`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        val modes = mutableListOf<RepeatMode>()

        // When
        repeat(times = 3) {
            controller.cycleRepeatMode()
            modes += currentState.repeatMode
        }

        // Then
        assertThat(modes).containsExactly(RepeatMode.All, RepeatMode.One, RepeatMode.Off).inOrder()
        assertThat(fakeExoPlayer.repeatMode).isEqualTo(Player.REPEAT_MODE_OFF)
    }

    @Test
    fun `GIVEN an album with a song queued by hand WHEN turning shuffle on THEN the queued song still plays next`() =
        runTest {
            // Given
            prepareScenario()
            playAlbum(startingAt = 1, songCount = 8)
            controller.addToQueue(listOf(song(id = 99)))

            // When
            controller.toggleShuffle()

            // Then
            assertThat(fakeExoPlayer.shuffleModeEnabled).isTrue()
            assertThat(currentState.isShuffleEnabled).isTrue()
            assertThat(currentState.currentSong?.id).isEqualTo(1L)
            assertThat(queuedSongIds().take(2)).containsExactly(1L, 99L).inOrder()
            assertThat(queuedSongIds().drop(2)).containsExactly(2L, 3L, 4L, 5L, 6L, 7L, 8L)
            assertThat(queuedSongIds().drop(2)).isNotEqualTo(listOf(2L, 3L, 4L, 5L, 6L, 7L, 8L))
            assertThat(currentState.entries.map { entry -> entry.id }).isEqualTo(fakeExoPlayer.mediaIds)
        }

    @Test
    fun `GIVEN shuffle is on WHEN turning it off THEN the album is back in order around the song playing`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1, songCount = 8)
        controller.toggleShuffle()
        val playing = currentState.entries[3]
        controller.skipTo(playing.id)

        // When
        controller.toggleShuffle()

        // Then
        val songId = playing.song.id
        assertThat(fakeExoPlayer.shuffleModeEnabled).isFalse()
        assertThat(currentState.unshuffledOrder).isEmpty()
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L).inOrder()
        assertThat(currentState.currentSong?.id).isEqualTo(songId)
        assertThat(currentState.currentIndex).isEqualTo(songId.toInt() - 1)
        assertThat(currentState.entries.map { entry -> entry.id }).isEqualTo(fakeExoPlayer.mediaIds)
    }

    /**
     * Told first, the player would hand the change to the queue from inside its own callback, and
     * the media session would hear of the queue's edits late, paired with where the player ended up:
     * on the eighth song of a timeline it had been told holds one, which it refuses.
     */
    @Test
    fun `GIVEN shuffle is on WHEN turning it off THEN the queue is put back before the player is told`() = runTest {
        // Given
        prepareScenario()
        controller.toggleShuffle()
        playAlbum(startingAt = 8, songCount = 8)

        // When
        controller.toggleShuffle()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L).inOrder()
        assertThat(currentState.currentIndex).isEqualTo(7)
        assertThat(currentState.isShuffleEnabled).isFalse()
        verifyOrder {
            fakeExoPlayer.player.replaceMediaItems(0, 0, match { items -> items.size == 7 })
            fakeExoPlayer.player.shuffleModeEnabled = false
        }
    }

    @Test
    fun `GIVEN the notification turned shuffle on WHEN the player says so THEN the queue follows it`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1, songCount = 8)
        fakeExoPlayer.shuffleModeEnabled = true

        // When
        playerListener.captured.onShuffleModeEnabledChanged(true)

        // Then
        assertThat(currentState.isShuffleEnabled).isTrue()
        assertThat(currentState.unshuffledOrder).hasSize(8)
        assertThat(queuedSongIds().drop(1)).isNotEqualTo(listOf(2L, 3L, 4L, 5L, 6L, 7L, 8L))
    }

    @Test
    fun `WHEN playing an album from the start THEN it starts on its first song`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.playFromStart(songs = albumSongs(count = 3), context = randomAccessMemories)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(currentState.currentSong?.id).isEqualTo(1L)
        assertThat(currentState.context).isEqualTo(randomAccessMemories)
        verify { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN shuffle and a song queued by hand WHEN playing an album from the start THEN it is shuffled behind it`() =
        runTest {
            // Given
            prepareScenario()
            controller.play(song = song(id = 50), songs = listOf(song(id = 50)), context = PlaybackContext.SingleSong)
            controller.toggleShuffle()
            controller.addToQueue(listOf(song(id = 99)))

            // When
            controller.playFromStart(songs = albumSongs(count = 8), context = randomAccessMemories)

            // Then
            val current = currentState.currentEntry
            assertThat(current?.source).isEqualTo(QueueSource.Context)
            assertThat(currentState.currentIndex).isEqualTo(0)
            assertThat(queuedSongIds()[1]).isEqualTo(99L)
            assertThat(queuedSongIds().filter { id -> id != 99L }).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)
            assertThat(currentState.unshuffledOrder).hasSize(8)
        }

    @Test
    fun `WHEN playing an empty album from the start THEN the player is left alone`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.playFromStart(songs = emptyList(), context = randomAccessMemories)

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.play() }
    }

    @Test
    fun `GIVEN a playlist WHEN playing one of its songs THEN the rest of it plays on with its context`() = runTest {
        // Given
        prepareScenario()

        // When
        controller.play(song = song(id = 2), songs = albumSongs(count = 4), context = roadTrip)

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L, 4L).inOrder()
        assertThat(currentState.currentSong?.id).isEqualTo(2L)
        assertThat(currentState.context).isEqualTo(roadTrip)
    }

    @Test
    fun `GIVEN a song queued by hand WHEN playing a song from a playlist THEN the queued song plays before the rest`() =
        runTest {
            // Given
            prepareScenario()
            playAlbum(startingAt = 1)
            controller.addToQueue(listOf(song(id = 99)))

            // When
            controller.play(song = song(id = 12), songs = playlistSongs(), context = roadTrip)

            // Then
            assertThat(queuedSongIds()).containsExactly(11L, 12L, 99L, 13L).inOrder()
            assertThat(currentState.currentSong?.id).isEqualTo(12L)
            assertThat(currentState.entries.map { entry -> entry.source })
                .containsExactly(
                    QueueSource.Context,
                    QueueSource.Context,
                    QueueSource.UserQueue,
                    QueueSource.Context,
                ).inOrder()
        }

    @Test
    fun `GIVEN a song queued by hand WHEN starting the liked songs THEN it plays right after the first`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        controller.addToQueue(listOf(song(id = 99)))

        // When
        controller.playFromStart(songs = playlistSongs(), context = PlaybackContext.LikedSongs)

        // Then
        assertThat(queuedSongIds()).containsExactly(11L, 99L, 12L, 13L).inOrder()
        assertThat(currentState.context).isEqualTo(PlaybackContext.LikedSongs)
    }

    @Test
    fun `GIVEN a playlist is playing WHEN turning shuffle on and off THEN it is back in its own order`() = runTest {
        // Given
        prepareScenario()
        controller.play(song = song(id = 1), songs = albumSongs(count = 8), context = roadTrip)
        controller.toggleShuffle()
        assertThat(queuedSongIds().drop(1)).isNotEqualTo(listOf(2L, 3L, 4L, 5L, 6L, 7L, 8L))

        // When
        controller.toggleShuffle()

        // Then
        assertThat(queuedSongIds()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L).inOrder()
        assertThat(currentState.context).isEqualTo(roadTrip)
    }

    @Test
    fun `GIVEN a saved playlist session WHEN restoring THEN the playlist is still the context`() = runTest {
        // Given
        prepareScenario()
        val session = PlaybackSession(
            entries = listOf(
                queueEntry(id = "a", song = song(id = 11)),
                queueEntry(id = "q", song = song(id = 99), source = QueueSource.UserQueue),
                queueEntry(id = "b", song = song(id = 12)),
            ),
            currentEntryId = "a",
            context = roadTrip,
            position = 3.seconds,
            repeatMode = RepeatMode.Off,
            isShuffleEnabled = false,
            unshuffledOrder = emptyList(),
            hasEnded = false,
        )

        // When
        controller.restore(session)

        // Then
        assertThat(currentState.context).isEqualTo(roadTrip)
        assertThat(queuedSongIds()).containsExactly(11L, 99L, 12L).inOrder()
    }

    @Test
    fun `WHEN the repeat mode changes on the player THEN it is published`() = runTest {
        // Given
        prepareScenario()
        playAlbum(startingAt = 1)
        fakeExoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        // When
        playerListener.captured.onRepeatModeChanged(Player.REPEAT_MODE_ALL)

        // Then
        assertThat(currentState.repeatMode).isEqualTo(RepeatMode.All)
        assertThat(currentState.hasNext).isTrue()
    }

    private fun albumSongs(count: Int): List<Song> = (1L..count).map { id -> song(id = id) }

    private fun playlistSongs(): List<Song> = listOf(song(id = 11), song(id = 12), song(id = 13))

    private fun endedSession(): PlaybackSession = PlaybackSession(
        entries = listOf(queueEntry(id = "a", song = song(id = 1))),
        currentEntryId = "a",
        context = PlaybackContext.SingleSong,
        position = 30.seconds,
        repeatMode = RepeatMode.Off,
        isShuffleEnabled = false,
        unshuffledOrder = emptyList(),
        hasEnded = true,
    )

    private fun playAlbum(
        startingAt: Long,
        songCount: Int = 3,
    ) {
        val songs = albumSongs(count = songCount)
        controller.play(
            song = songs.first { song -> song.id == startingAt },
            songs = songs,
            context = randomAccessMemories,
        )
    }

    private fun queuedSongIds(): List<Long> = currentState.entries
        .map { entry -> entry.song.id }

    private fun TestScope.prepareScenario() {
        controller = ExoPlayerPlaybackController(
            player = fakeExoPlayer.player,
            serviceLauncher = { serviceLaunches++ },
            queue = PlaybackQueue(
                player = fakeExoPlayer.player,
                mediaItemFactory = ::createTestMediaItem,
                timelineFactory = QueueTimelineFactory(idGenerator = UuidIdGenerator(), random = Random(seed = 7)),
            ),
            scope = backgroundScope,
        )
        verify { fakeExoPlayer.player.addListener(capture(playerListener)) }
    }
}
