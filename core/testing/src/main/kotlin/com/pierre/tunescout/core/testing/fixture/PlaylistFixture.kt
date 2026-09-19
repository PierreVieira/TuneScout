package com.pierre.tunescout.core.testing.fixture

import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Playlist

fun playlist(
    id: Long = 1,
    name: String = "Road trip",
    songCount: Int = 0,
    artworks: List<Artwork> = emptyList(),
): Playlist = Playlist(
    id = id,
    name = name,
    songCount = songCount,
    artworks = artworks,
)
