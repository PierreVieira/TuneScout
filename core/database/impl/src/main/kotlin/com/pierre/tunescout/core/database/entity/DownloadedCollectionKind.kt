package com.pierre.tunescout.core.database.entity

/**
 * What [DownloadedCollectionEntity.kind] stores. The queries in `DownloadDao` spell the same names
 * out, since a query cannot read a Kotlin constant: renaming one means renaming it there too.
 */
internal enum class DownloadedCollectionKind {
    Favorites,
    Playlist,
    Album,
}
