package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

/**
 * A song the user asked to take off the device even though a collection still wants it. It stays
 * excluded until the song is asked for again on its own, which is what downloading it clears.
 *
 * @property songId the song.
 * @property excludedAt when the user asked to take it off the device.
 */
@Entity(
    tableName = "downloaded_song_exclusions",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class DownloadedSongExclusionEntity(
    @PrimaryKey val songId: Long,
    val excludedAt: Long,
)
