package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity

/**
 * A collection the user asked to keep on the device. The row holds the collection, not its songs:
 * which songs that means is read from the collection every time, so a song added to a downloaded
 * playlist is downloaded with it and one taken out of it is not kept for it any more.
 *
 * There is no key to the collection itself, since the three kinds live in three tables; deleting a
 * playlist deletes its row here in the same transaction.
 *
 * @property kind which table [collectionId] points into, one of the [DownloadedCollectionKind] names.
 * @property collectionId the album or the playlist, and 0 for the liked songs.
 * @property requestedAt when the user asked for it.
 */
@Entity(
    tableName = "downloaded_collections",
    primaryKeys = ["kind", "collectionId"],
)
internal data class DownloadedCollectionEntity(
    val kind: String,
    val collectionId: Long,
    val requestedAt: Long,
)
