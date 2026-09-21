package com.pierre.tunescout.core.testing.fake

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback

/**
 * A [SongPlayback] that answers whatever the test tells it to and keeps what it was asked. The rule
 * behind the real answers is tested where it lives, so a screen's test is left with what it alone
 * decides: what it asks for, and what it does with each answer.
 *
 * @property outcome the answer to the next requests, which a test changes between them.
 */
class FakeSongPlayback(
    var outcome: SongPlayOutcome = SongPlayOutcome.Started,
) : SongPlayback {
    val requests: MutableList<SongPlayRequest> = mutableListOf()

    override fun request(
        song: Song,
        nowPlaying: NowPlaying?,
        queue: List<Song>,
        context: PlaybackContext,
    ): SongPlayOutcome {
        requests += SongPlayRequest(song = song, nowPlaying = nowPlaying, queue = queue, context = context)
        return outcome
    }
}

/**
 * One call to [SongPlayback.request], as the screen made it.
 *
 * @property song the song whose row was tapped.
 * @property nowPlaying the song the screen marked as the player's when it was tapped.
 * @property queue what the screen wanted behind [song].
 * @property context where the screen was playing from.
 */
data class SongPlayRequest(
    val song: Song,
    val nowPlaying: NowPlaying?,
    val queue: List<Song>,
    val context: PlaybackContext,
)
