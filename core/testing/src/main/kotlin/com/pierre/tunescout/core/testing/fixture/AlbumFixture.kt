package com.pierre.tunescout.core.testing.fixture

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song

fun album(
    id: Long = 10,
    title: String = "Random Access Memories",
    artistName: String = "Daft Punk",
    artworkUrl: String = "https://example.com/album/$id.jpg",
    songs: List<Song> = listOf(
        song(id = 1, albumId = id, trackNumber = 1),
        song(id = 2, albumId = id, trackNumber = 2),
    ),
): Album = Album(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artworkUrl,
    songs = songs,
)
