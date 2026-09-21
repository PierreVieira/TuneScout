package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song

/**
 * Starts a whole context — an album — the way its play button does, rather than from one of its
 * rows: from its first song, or from any of them while shuffle is on.
 */
fun interface ContextStarter {
    fun playFromStart(
        songs: List<Song>,
        context: PlaybackContext,
    )
}
