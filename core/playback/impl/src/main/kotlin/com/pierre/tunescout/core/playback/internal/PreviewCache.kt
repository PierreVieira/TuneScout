package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.Song

/**
 * The previews already on the device. With no connection only these can play, which is what
 * [ConnectivityPlayableSongs] asks before a screen hands a song to the player.
 */
internal fun interface PreviewCache {
    /**
     * @return true when the whole of [song]'s preview is on disk, and false when none or only part of
     * it is — a preview stopped halfway would still stop halfway offline.
     */
    fun isCached(song: Song): Boolean
}
