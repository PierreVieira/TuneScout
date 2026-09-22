package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

/**
 * @property items everything in the library.
 * @property viewMode whether the items are laid out as a list or a grid.
 * @property gridColumns how many items a row of the grid holds; the list ignores it.
 * @property filters the chips that are on. None on means all of the library.
 * @property downloadedKeys the items the user asked to keep on the device as a whole.
 */
data class LibraryUiState(
    val items: List<LibraryItemUiModel>,
    val viewMode: LibraryViewMode,
    val gridColumns: LibraryGridColumns,
    val filters: Set<LibraryFilter>,
    val downloadedKeys: Set<LibraryItemKey>,
) {
    /**
     * An item shows when it matches every chip that is on. The songs downloaded one by one only ever
     * show under the downloaded chip.
     */
    val filteredItems: List<LibraryItemUiModel>
        get() = items.filter { item ->
            val isListed = item !is LibraryItemUiModel.DownloadedSongs || LibraryFilter.DOWNLOADED in filters
            isListed && filters.all { filter -> item.isMatching(filter) }
        }

    /**
     * The chips in the order they are drawn: the ones that are on lead, and once a kind is picked the
     * other kind steps aside, since picking it would only swap them — the way Spotify's bar reads.
     */
    val visibleFilters: List<LibraryFilter>
        get() {
            val hasKind = filters.any(LibraryFilter::isKind)
            val (on, off) = LibraryFilter.entries.partition { filter -> filter in filters }
            return on + off.filterNot { filter -> hasKind && filter.isKind }
        }

    val hasFilters: Boolean
        get() = filters.isNotEmpty()

    /** Whether the downloaded chip is on and nothing downloaded matches it, which the screen says in words. */
    val isDownloadedEmpty: Boolean
        get() = LibraryFilter.DOWNLOADED in filters && filteredItems.isEmpty()

    /** Whether the albums chip is on and the user has no albums, which the screen says in words. */
    val isAlbumsEmpty: Boolean
        get() = LibraryFilter.ALBUMS in filters && filteredItems.isEmpty()

    /**
     * @return whether the user asked to keep [item] as a whole. The songs downloaded one by one are
     * downloaded by definition, whatever the collections say.
     */
    fun isDownloaded(item: LibraryItemUiModel): Boolean =
        item is LibraryItemUiModel.DownloadedSongs || item.key in downloadedKeys

    private fun LibraryItemUiModel.isMatching(filter: LibraryFilter): Boolean = when (filter) {
        LibraryFilter.DOWNLOADED -> isDownloaded(this)
        LibraryFilter.PLAYLISTS, LibraryFilter.ALBUMS -> kind == filter
    }
}
