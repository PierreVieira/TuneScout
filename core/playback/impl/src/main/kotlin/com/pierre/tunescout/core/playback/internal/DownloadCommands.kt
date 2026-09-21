package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.Song

/** What the player is told to fetch and to drop, one song at a time. */
internal interface DownloadCommands {
    fun add(song: Song)

    fun remove(songId: Long)
}
