package com.pierre.tunescout.core.model

data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String,
    val songs: List<Song>,
)
