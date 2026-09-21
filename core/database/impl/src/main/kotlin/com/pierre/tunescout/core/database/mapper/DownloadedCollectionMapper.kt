package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedCollectionKind
import com.pierre.tunescout.core.model.LibraryItemKey

private const val FAVORITES_COLLECTION_ID = 0L

internal fun LibraryItemKey.toDownloadedCollectionEntity(requestedAt: Long): DownloadedCollectionEntity =
    DownloadedCollectionEntity(
        kind = toDownloadedCollectionKind().name,
        collectionId = toDownloadedCollectionId(),
        requestedAt = requestedAt,
    )

internal fun LibraryItemKey.toDownloadedCollectionKind(): DownloadedCollectionKind = when (this) {
    LibraryItemKey.Favorites -> DownloadedCollectionKind.Favorites
    is LibraryItemKey.Playlist -> DownloadedCollectionKind.Playlist
    is LibraryItemKey.Album -> DownloadedCollectionKind.Album
}

internal fun LibraryItemKey.toDownloadedCollectionId(): Long = when (this) {
    LibraryItemKey.Favorites -> FAVORITES_COLLECTION_ID
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
