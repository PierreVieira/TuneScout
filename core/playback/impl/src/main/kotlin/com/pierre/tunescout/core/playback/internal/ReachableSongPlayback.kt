package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback

/**
 * Starts only songs the player can reach, and never the one it is already on: asking for that one
 * again would drop the position the listener is at and start it over from the beginning.
 *
 * The queue is filtered too, so what plays after the song is what the player can reach as well.
 *
 * @property playbackStarter what the songs that survive both rules are handed to.
 * @property playableSongs which songs the player can reach right now.
 */
internal class ReachableSongPlayback(
    private val playbackStarter: PlaybackStarter,
    private val playableSongs: PlayableSongs,
) : SongPlayback {
    override fun request(
        song: Song,
        nowPlaying: NowPlaying?,
        queue: List<Song>,
        context: PlaybackContext,
    ): SongPlayOutcome = when {
        nowPlaying.isOn(song.id) -> SongPlayOutcome.AlreadyPlaying

        !playableSongs.isPlayable(song) -> SongPlayOutcome.Unavailable

        else -> {
            playbackStarter.play(
                song = song,
                songs = playableSongs.filterPlayable(queue),
                context = context,
            )
            SongPlayOutcome.Started
        }
    }
}
