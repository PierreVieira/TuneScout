package com.pierre.tunescout.core.testing.fixture

import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun song(
    id: Long = 1,
    title: String = "Get Lucky",
    artistName: String = "Daft Punk",
    albumId: Long = 10,
    albumTitle: String = "Random Access Memories",
    artwork: Artwork = Artwork("https://example.com/art/$id/100x100bb.jpg"),
    previewUrl: String = "https://example.com/preview/$id.m4a",
    duration: Duration = 30.seconds,
    trackNumber: Int = 1,
): Song = Song(
    id = id,
    title = title,
    artistName = artistName,
    albumId = albumId,
    albumTitle = albumTitle,
    artwork = artwork,
    previewUrl = previewUrl,
    duration = duration,
    trackNumber = trackNumber,
)
