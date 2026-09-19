package com.pierre.tunescout.core.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val artworks: List<Artwork>,
)
