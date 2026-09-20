package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.Song

interface Enqueuer {
    /**
     * Takes over from whatever is playing: [songs] go in right after the current one and the first
     * of them starts at once, so the rest of the queue is pushed back rather than thrown away.
     */
    fun playNow(songs: List<Song>)

    fun queueNext(songs: List<Song>)

    fun addToQueue(songs: List<Song>)
}
