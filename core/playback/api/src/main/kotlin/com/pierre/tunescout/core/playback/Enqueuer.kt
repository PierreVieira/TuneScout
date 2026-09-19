package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.Song

interface Enqueuer {
    fun queueNext(songs: List<Song>)

    fun addToQueue(songs: List<Song>)
}
