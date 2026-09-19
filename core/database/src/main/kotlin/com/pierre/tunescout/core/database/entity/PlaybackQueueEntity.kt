package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "playback_queue",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["songId"]), Index(value = ["position"])],
)
internal data class PlaybackQueueEntity(
    @PrimaryKey val entryId: String,
    val position: Int,
    val songId: Long,
    val source: String,
)
