package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.ReachableSongPlayback
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.Test

class ReachableSongPlaybackTest {
    private val reachable = song(id = 1)
    private val unreachable = song(id = 2)
    private lateinit var started: MutableList<Triple<Song, List<Song>, PlaybackContext>>
    private lateinit var songPlayback: ReachableSongPlayback

    @Test
    fun `GIVEN the song the player is on WHEN asking for it THEN says so and starts nothing`() {
        // Given
        prepareScenario()

        // When
        val outcome = play(song = reachable, nowPlaying = NowPlaying(songId = 1, isPlaying = true))

        // Then
        assertThat(outcome).isEqualTo(SongPlayOutcome.AlreadyPlaying)
        assertThat(started).isEmpty()
    }

    @Test
    fun `GIVEN the song the player is on is paused WHEN asking for it THEN still starts nothing`() {
        // Given
        prepareScenario()

        // When
        val outcome = play(song = reachable, nowPlaying = NowPlaying(songId = 1, isPlaying = false))

        // Then
        assertThat(outcome).isEqualTo(SongPlayOutcome.AlreadyPlaying)
        assertThat(started).isEmpty()
    }

    @Test
    fun `GIVEN the player is on another song WHEN asking for a reachable one THEN starts it`() {
        // Given
        prepareScenario()

        // When
        val outcome = play(song = reachable, nowPlaying = NowPlaying(songId = 99, isPlaying = true))

        // Then
        assertThat(outcome).isEqualTo(SongPlayOutcome.Started)
        assertThat(started).hasSize(1)
        assertThat(started.single().first).isEqualTo(reachable)
    }

    @Test
    fun `GIVEN nothing is playing WHEN asking for a song the player cannot reach THEN refuses it`() {
        // Given
        prepareScenario()

        // When
        val outcome = play(song = unreachable, nowPlaying = null)

        // Then
        assertThat(outcome).isEqualTo(SongPlayOutcome.Unavailable)
        assertThat(started).isEmpty()
    }

    @Test
    fun `GIVEN a queue the player can only half reach WHEN starting it THEN queues only what it reaches`() {
        // Given
        prepareScenario()

        // When
        val outcome = play(song = reachable, nowPlaying = null, queue = listOf(reachable, unreachable))

        // Then
        assertThat(outcome).isEqualTo(SongPlayOutcome.Started)
        assertThat(started.single().second).containsExactly(reachable)
    }

    @Test
    fun `GIVEN a context WHEN starting a song THEN hands it to the player untouched`() {
        // Given
        prepareScenario()
        val context = PlaybackContext.Album(id = 10, title = "Random Access Memories")

        // When
        play(song = reachable, nowPlaying = null, context = context)

        // Then
        assertThat(started.single().third).isEqualTo(context)
    }

    private fun play(
        song: Song,
        nowPlaying: NowPlaying?,
        queue: List<Song> = listOf(song),
        context: PlaybackContext = PlaybackContext.SingleSong,
    ): SongPlayOutcome = songPlayback.request(
        song = song,
        nowPlaying = nowPlaying,
        queue = queue,
        context = context,
    )

    private fun prepareScenario() {
        started = mutableListOf()
        songPlayback = ReachableSongPlayback(
            playbackStarter = { song, songs, context -> started += Triple(song, songs, context) },
            playableSongs = { song -> song.id == reachable.id },
        )
    }
}
