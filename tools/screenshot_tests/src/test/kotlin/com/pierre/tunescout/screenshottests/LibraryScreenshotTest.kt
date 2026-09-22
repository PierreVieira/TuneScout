package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.content.LibraryContent
import com.pierre.tunescout.feature.library.presentation.content.LibrarySearchContent
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiState
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.instantCrush
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.touch
import org.junit.Test

internal class LibraryScreenshotTest : ScreenshotTest() {
    private val items = listOf(
        LibraryItemUiModel.Favorites(
            songCount = recentlyPlayed.size,
            artworks = recentlyPlayed.map { song -> song.artwork },
        ),
        LibraryItemUiModel.Playlist(
            id = 1,
            name = "Late night drive",
            songCount = 3,
            artworks = listOf(getLucky, instantCrush, touch).map { song -> song.artwork },
        ),
        LibraryItemUiModel.Playlist(id = 2, name = "To listen later", songCount = 0, artworks = emptyList()),
        LibraryItemUiModel.Album(
            id = randomAccessMemories.id,
            title = randomAccessMemories.title,
            artistName = randomAccessMemories.artistName,
            artwork = randomAccessMemories.artwork,
        ),
    )
    private val library =
        LibraryUiState(
            items = items,
            viewMode = LibraryViewMode.LIST,
            filters = emptySet(),
            downloadedKeys = emptySet(),
        )

    @Test
    fun list() {
        snapshot(name = "list", variants = ScreenshotVariant.all) {
            LibraryContent(uiState = library, onEvent = {})
        }
    }

    @Test
    fun grid() {
        snapshot(name = "grid") {
            LibraryContent(uiState = library.copy(viewMode = LibraryViewMode.GRID), onEvent = {})
        }
    }

    @Test
    fun filteredByAlbums() {
        snapshot(name = "filtered_by_albums") {
            LibraryContent(uiState = library.copy(filters = setOf(LibraryFilter.ALBUMS)), onEvent = {})
        }
    }

    @Test
    fun filteredByDownloaded() {
        snapshot(name = "filtered_by_downloaded") {
            LibraryContent(
                uiState = library.copy(
                    items = items.take(1) +
                        LibraryItemUiModel.DownloadedSongs(
                            songCount = 2,
                            artworks = listOf(getLucky, touch).map { song -> song.artwork },
                        ) +
                        items.drop(1),
                    filters = setOf(LibraryFilter.DOWNLOADED),
                    downloadedKeys = setOf(LibraryItemKey.Album(albumId = randomAccessMemories.id)),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun filteredByDownloadedWithNothingDownloaded() {
        snapshot(name = "filtered_by_downloaded_empty") {
            LibraryContent(uiState = library.copy(filters = setOf(LibraryFilter.DOWNLOADED)), onEvent = {})
        }
    }

    @Test
    fun empty() {
        snapshot(name = "empty") {
            LibraryContent(uiState = library.copy(items = emptyList()), onEvent = {})
        }
    }

    @Test
    fun searchRecent() {
        snapshot(name = "search_recent") {
            LibrarySearchContent(
                uiState = LibrarySearchUiState(query = "", items = items, recentSearches = items.take(2)),
                onEvent = {},
            )
        }
    }

    @Test
    fun searchResults() {
        snapshot(name = "search_results") {
            LibrarySearchContent(
                uiState = LibrarySearchUiState(query = "late", items = items, recentSearches = emptyList()),
                onEvent = {},
            )
        }
    }

    @Test
    fun searchWithoutResults() {
        snapshot(name = "search_without_results") {
            LibrarySearchContent(
                uiState = LibrarySearchUiState(
                    query = "nothing like this",
                    items = items,
                    recentSearches = emptyList(),
                ),
                onEvent = {},
            )
        }
    }
}
