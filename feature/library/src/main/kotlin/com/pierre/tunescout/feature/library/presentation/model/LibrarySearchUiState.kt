package com.pierre.tunescout.feature.library.presentation.model

data class LibrarySearchUiState(
    val query: String,
    val items: List<LibraryItemUiModel>,
    val recentSearches: List<LibraryItemUiModel>,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
