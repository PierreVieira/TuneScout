package com.pierre.tunescout.core.playback

import androidx.media3.common.Player
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.core.utils.UuidIdGenerator
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class ExoPlayerPlaybackControllerTest {
    private val randomAccessMemories = PlaybackContext.Album(id = 10, title = "Random Access Memories")

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
            isRepeatEnabled = true,
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
                isRepeatEnabled = false,
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
                isRepeatEnabled = false,
            ),
        )

        // Then
        verify(exactly = 0) { fakeExoPlayer.player.setMediaItems(any(), any<Int>(), any<Long>()) }
    }

    private fun playAlbum(startingAt: Long) {
        val songs = listOf(song(id = 1), song(id = 2), song(id = 3))
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
                idGenerator = UuidIdGenerator(),
            ),
            scope = backgroundScope,
        )
        verify { fakeExoPlayer.player.addListener(capture(playerListener)) }
    }
}
