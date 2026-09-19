package com.pierre.tunescout.core.playback.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.pierre.tunescout.core.model.QueueEntry

internal fun interface MediaItemFactory {
    fun createMediaItem(entry: QueueEntry): MediaItem
}

internal class AndroidMediaItemFactory : MediaItemFactory {
    override fun createMediaItem(entry: QueueEntry): MediaItem = MediaItem
        .Builder()
        .setMediaId(entry.id)
        .setUri(entry.song.previewUrl)
        .setMediaMetadata(
            MediaMetadata
                .Builder()
                .setTitle(entry.song.title)
                .setArtist(entry.song.artistName)
                .setAlbumTitle(entry.song.albumTitle)
                .setArtworkUri(
                    entry.song.artwork.mediumUrl
                        .toUri(),
                ).build(),
        ).build()
}
