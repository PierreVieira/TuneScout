package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

/**
 * Where the user put one of an album's tracks. An album has no row here until it is reordered, and
 * then keeps its order through every refresh: the tracks come from the API, the order does not.
 *
 * There is no key to the album itself: one put together from the saved tracks has no album row to
 * point at, and can be reordered all the same.
 *
 * @property albumId the album the track is reordered in.
 * @property songId the track.
 * @property position where the track goes, counting from zero.
 */
@Entity(
    tableName = "album_track_order",
    primaryKeys = ["albumId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["songId"])],
)
internal data class AlbumTrackOrderEntity(
    val albumId: Long,
    val songId: Long,
    val position: Int,
)
