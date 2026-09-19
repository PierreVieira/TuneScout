package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.MediaItem
import com.pierre.tunescout.core.model.QueueEntry

internal fun interface MediaItemFactory {
    fun createMediaItem(entry: QueueEntry): MediaItem
}
