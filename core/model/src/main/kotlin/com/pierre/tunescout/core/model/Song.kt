package com.pierre.tunescout.core.model

import kotlin.time.Duration

data class Song(
    val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val artwork: Artwork,
    val previewUrl: String,
    val duration: Duration,
    val trackNumber: Int,
)
