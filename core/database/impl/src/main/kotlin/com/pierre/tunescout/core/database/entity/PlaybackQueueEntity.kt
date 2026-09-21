package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * One entry of the saved queue.
 *
 * @property entryId the entry's own id, which tells two entries of the same song apart.
 * @property position where the entry plays in the queue.
 * @property songId the song the entry plays.
 * @property source the name of the queue source the entry came from.
 * @property unshuffledPosition where a context entry sits in the context's own order while the queue
 * is shuffled, so turning shuffle off after a restart can still put it back. Null otherwise.
 */
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
    val unshuffledPosition: Int?,
)
