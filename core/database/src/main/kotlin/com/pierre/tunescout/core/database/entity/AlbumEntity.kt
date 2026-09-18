package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "albums")
internal data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String,
    val cachedAt: Long,
)
