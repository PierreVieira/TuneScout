package com.pierre.tunescout.feature.library.domain.usecase

data class LibrarySearchUseCases(
    val observePlaylists: ObservePlaylists,
    val observeFavorites: ObserveFavorites,
    val observeFavoriteAlbums: ObserveFavoriteAlbums,
    val observeRecentSearches: ObserveRecentLibrarySearches,
    val recordSearch: RecordLibrarySearch,
    val removeSearch: RemoveLibrarySearch,
)
