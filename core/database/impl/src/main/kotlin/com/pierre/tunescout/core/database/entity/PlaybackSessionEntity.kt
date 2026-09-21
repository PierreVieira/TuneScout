package com.pierre.tunescout.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

internal const val PLAYBACK_SESSION_ID = 0

/**
 * @property id always [PLAYBACK_SESSION_ID]: there is one session.
 * @property currentEntryId the queue entry the player is on.
 * @property positionMillis how far into it the player is.
 * @property repeatMode the [com.pierre.tunescout.core.model.RepeatMode] by name.
 * @property isShuffleEnabled whether the context plays in a random order.
 * @property contextType which kind of [com.pierre.tunescout.core.model.PlaybackContext] the queue
 * was built on, by name.
 * @property contextId the album or playlist it was built on, for the kinds that have one.
 * @property contextTitle the album's title or the playlist's name when it started.
 * @property hasEnded whether the current song had played to its end.
 */
@Entity(tableName = "playback_session")
internal data class PlaybackSessionEntity(
    @PrimaryKey val id: Int,
    val currentEntryId: String?,
    val positionMillis: Long,
    val repeatMode: String,
    val isShuffleEnabled: Boolean,
    val contextType: String,
    val contextId: Long?,
    val contextTitle: String?,
    @ColumnInfo(defaultValue = "0") val hasEnded: Boolean,
)
