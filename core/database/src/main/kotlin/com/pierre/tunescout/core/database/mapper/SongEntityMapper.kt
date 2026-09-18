package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration.Companion.milliseconds

internal fun SongEntity.toSong(): Song = Song(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    duration = durationMillis.milliseconds,
    trackNumber = trackNumber,
)

internal fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    durationMillis = duration.inWholeMilliseconds,
    trackNumber = trackNumber,
)
