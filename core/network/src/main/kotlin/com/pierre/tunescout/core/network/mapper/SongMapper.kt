package com.pierre.tunescout.core.network.mapper

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.dto.ResultDto
import kotlin.time.Duration.Companion.milliseconds

private const val TRACK_WRAPPER = "track"
private const val SONG_KIND = "song"

internal fun ResultDto.toSongOrNull(): Song? {
    if (wrapperType != TRACK_WRAPPER || kind != SONG_KIND) return null
    return Song(
        id = trackId ?: return null,
        title = trackName ?: return null,
        artistName = artistName ?: return null,
        albumId = collectionId ?: return null,
        albumTitle = collectionName.orEmpty(),
        artworkUrl = artworkUrl100.orEmpty(),
        previewUrl = previewUrl ?: return null,
        duration = (trackTimeMillis ?: 0L).milliseconds,
        trackNumber = trackNumber ?: 0,
    )
}
