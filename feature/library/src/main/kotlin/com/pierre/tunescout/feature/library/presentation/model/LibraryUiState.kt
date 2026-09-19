package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

data class LibraryUiState(
    val items: List<LibraryItemUiModel>,
    val viewMode: LibraryViewMode,
    val filter: LibraryFilter?,
) {
    /** No chip picked means no filter, which is how the chips read: nothing selected, all of it. */
    val filteredItems: List<LibraryItemUiModel>
        get() = items.filter { item -> filter == null || item.filter == filter }
}
