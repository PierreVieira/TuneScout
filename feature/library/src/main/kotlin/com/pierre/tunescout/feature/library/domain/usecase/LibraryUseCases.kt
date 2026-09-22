package com.pierre.tunescout.feature.library.domain.usecase

data class LibraryUseCases(
    val observePlaylists: ObservePlaylists,
    val observeFavorites: ObserveFavorites,
    val observeFavoriteAlbums: ObserveFavoriteAlbums,
    val observeViewMode: ObserveLibraryViewMode,
    val setViewMode: SetLibraryViewMode,
    val observeCollectionDownloads: ObserveCollectionDownloads,
    val observeDownloadedSongs: ObserveDownloadedSongs,
)
