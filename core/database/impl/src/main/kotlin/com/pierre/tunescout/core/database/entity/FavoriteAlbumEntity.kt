package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "favorite_albums",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["favoritedAt"])],
)
internal data class FavoriteAlbumEntity(
    @PrimaryKey val albumId: Long,
    val favoritedAt: Long,
)
