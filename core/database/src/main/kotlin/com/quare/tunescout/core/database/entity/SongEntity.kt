package com.quare.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "songs", indices = [Index(value = ["albumId"])])
internal data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val artworkUrl: String,
    val previewUrl: String,
    val durationMillis: Long,
    val trackNumber: Int,
)
