package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

/**
 * A song the user asked to keep on the device on its own, rather than as part of a collection.
 *
 * @property songId the song.
 * @property requestedAt when the user asked for it.
 */
@Entity(
    tableName = "downloaded_songs",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class DownloadedSongEntity(
    @PrimaryKey val songId: Long,
    val requestedAt: Long,
)
