package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.Song

/**
 * The previews already on the device. With no connection only these can play, so a screen asks
 * before handing a song to the player instead of letting the player fail on it.
 */
fun interface PreviewCache {
    /**
     * @return true when the whole of [song]'s preview is on disk, and false when none or only part of
     * it is — a preview stopped halfway would still stop halfway offline.
     */
    fun isCached(song: Song): Boolean
}
