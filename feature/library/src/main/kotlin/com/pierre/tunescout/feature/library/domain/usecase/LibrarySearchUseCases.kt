package com.pierre.tunescout.feature.library.domain.usecase

data class LibrarySearchUseCases(
    val observePlaylists: ObservePlaylists,
    val observeFavorites: ObserveFavorites,
    val observeRecentSearches: ObserveRecentLibrarySearches,
    val recordSearch: RecordLibrarySearch,
    val removeSearch: RemoveLibrarySearch,
)
