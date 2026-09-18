package com.pierre.tunescout.core.model

data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artwork: Artwork,
    val songs: List<Song>,
)
