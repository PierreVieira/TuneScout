package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

/**
 * @property items everything in the library.
 * @property viewMode whether the items are laid out as a list or a grid.
 * @property filter the kind of item the chips narrow the library to, if any.
 * @property downloadedKeys the items the user asked to keep on the device as a whole.
 */
data class LibraryUiState(
    val items: List<LibraryItemUiModel>,
    val viewMode: LibraryViewMode,
    val filter: LibraryFilter?,
    val downloadedKeys: Set<LibraryItemKey>,
) {
    /** No chip picked means no filter, which is how the chips read: nothing selected, all of it. */
    val filteredItems: List<LibraryItemUiModel>
        get() = items.filter { item -> filter == null || item.filter == filter }
}
