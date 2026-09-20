package com.pierre.tunescout.core.testing.fixture

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song

fun album(
    id: Long = 10,
    title: String = "Random Access Memories",
    artistName: String = "Daft Punk",
    artwork: Artwork = Artwork("https://example.com/album/$id/100x100bb.jpg"),
    songs: List<Song> = listOf(
        song(id = 1, albumId = id, trackNumber = 1),
        song(id = 2, albumId = id, trackNumber = 2),
    ),
    isComplete: Boolean = true,
): Album = Album(
    id = id,
    title = title,
    artistName = artistName,
    artwork = artwork,
    songs = songs,
    isComplete = isComplete,
)

fun albumSummary(
    id: Long = 10,
    title: String = "Random Access Memories",
    artistName: String = "Daft Punk",
    artwork: Artwork = Artwork("https://example.com/art/$id/100x100bb.jpg"),
): AlbumSummary = AlbumSummary(
    id = id,
    title = title,
    artistName = artistName,
    artwork = artwork,
)
