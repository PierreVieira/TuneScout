package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedCollectionKind
import com.pierre.tunescout.core.model.LibraryItemKey

/** The liked songs and the downloaded songs are one of a kind, so neither points at a row of its own. */
private const val NO_COLLECTION_ID = 0L

/**
 * @return the row that asks for the collection, or null for the downloaded songs: each of them is
 * already kept by its own request, so there is no collection to ask for.
 */
internal fun LibraryItemKey.toDownloadedCollectionEntityOrNull(requestedAt: Long): DownloadedCollectionEntity? {
    val kind = toDownloadedCollectionKindOrNull() ?: return null
    return DownloadedCollectionEntity(
        kind = kind.name,
        collectionId = toDownloadedCollectionId(),
        requestedAt = requestedAt,
    )
}

/** @return how the collection is stored, or null for the downloaded songs, which are not stored as one. */
internal fun LibraryItemKey.toDownloadedCollectionKindOrNull(): DownloadedCollectionKind? = when (this) {
    LibraryItemKey.Favorites -> DownloadedCollectionKind.Favorites
    is LibraryItemKey.Playlist -> DownloadedCollectionKind.Playlist
    is LibraryItemKey.Album -> DownloadedCollectionKind.Album
    LibraryItemKey.DownloadedSongs -> null
}

internal fun LibraryItemKey.toDownloadedCollectionId(): Long = when (this) {
    LibraryItemKey.Favorites, LibraryItemKey.DownloadedSongs -> NO_COLLECTION_ID
    is LibraryItemKey.Playlist -> playlistId
    is LibraryItemKey.Album -> albumId
}

/** @return the collection the row points at, or null for a kind this version does not know. */
internal fun DownloadedCollectionEntity.toLibraryItemKeyOrNull(): LibraryItemKey? =
    when (DownloadedCollectionKind.entries.find { entry -> entry.name == kind }) {
        DownloadedCollectionKind.Favorites -> LibraryItemKey.Favorites
        DownloadedCollectionKind.Playlist -> LibraryItemKey.Playlist(playlistId = collectionId)
        DownloadedCollectionKind.Album -> LibraryItemKey.Album(albumId = collectionId)
        null -> null
    }
