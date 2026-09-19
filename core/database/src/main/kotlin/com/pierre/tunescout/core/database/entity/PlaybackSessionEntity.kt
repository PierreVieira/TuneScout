package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

internal const val PLAYBACK_SESSION_ID = 0

@Entity(tableName = "playback_session")
internal data class PlaybackSessionEntity(
    @PrimaryKey val id: Int,
    val currentEntryId: String?,
    val positionMillis: Long,
    val isRepeatEnabled: Boolean,
    val contextAlbumId: Long?,
    val contextAlbumTitle: String?,
)
