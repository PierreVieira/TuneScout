package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song

fun interface PlaybackStarter {
    fun play(
        song: Song,
        songs: List<Song>,
        context: PlaybackContext,
    )
}
