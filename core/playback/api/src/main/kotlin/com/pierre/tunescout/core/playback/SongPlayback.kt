package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song

/**
 * The rule every list follows when a row is tapped — the recents, the search results, a collection,
 * an album. All three answer a tap the same way, so the decision lives here instead of once per
 * screen, and each screen is left with only its own half of it: the song already playing is one the
 * screen takes the user to, and one the player cannot reach is refused in the screen's own words.
 *
 * The player screen is deliberately not a caller: it owns the song it shows, so a tap there resumes
 * it rather than taking the user anywhere, and it refuses a song it cannot reach before anything
 * else.
 */
fun interface SongPlayback {
    /**
     * @return what came of asking for [song], with [queue] behind it in [context] when it starts.
     *  [nowPlaying] is the song the asking screen marks as the player's, which decides
     *  [SongPlayOutcome.AlreadyPlaying].
     */
    fun request(
        song: Song,
        nowPlaying: NowPlaying?,
        queue: List<Song>,
        context: PlaybackContext,
    ): SongPlayOutcome
}
