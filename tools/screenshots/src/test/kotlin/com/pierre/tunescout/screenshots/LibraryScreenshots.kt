package com.pierre.tunescout.screenshots

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.content.LibraryContent
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import org.junit.Test

internal class LibraryScreenshots : ReadmeScreenshotsTest() {
    private val library = LibraryUiState(
        items = listOf(
            LibraryItemUiModel.Favorites(
                songCount = recentlyPlayed.size,
                artworks = recentlyPlayed.map { song -> song.artwork },
            ),
            LibraryItemUiModel.Playlist(
                id = 1,
                name = "Late night drive",
                songCount = 4,
                artworks = listOf(getLucky, instantCrush, touch, giorgioByMoroder).map { song -> song.artwork },
            ),
            LibraryItemUiModel.Playlist(
                id = 2,
                name = "Discovery on repeat",
                songCount = 2,
                artworks = listOf(oneMoreTime, harderBetterFasterStronger).map { song -> song.artwork },
            ),
            LibraryItemUiModel.Playlist(id = 3, name = "To listen later", songCount = 0, artworks = emptyList()),
            LibraryItemUiModel.Album(
                id = randomAccessMemories.id,
                title = randomAccessMemories.title,
                artistName = randomAccessMemories.artistName,
                artwork = randomAccessMemories.artwork,
            ),
        ),
        viewMode = LibraryViewMode.LIST,
        filter = null,
    )

    @Test
    fun library() {
        capture(
            fileName = "library",
            title = "Keep what you like",
            description = "Liked songs, the playlists you make and the albums you keep",
        ) {
            LibraryContent(uiState = library, onEvent = {})
        }
    }

    @Test
    fun libraryGrid() {
        capture(
            fileName = "library_grid",
            title = "Or see them as covers",
            description = "The same library in a grid, remembered between launches",
        ) {
            LibraryContent(uiState = library.copy(viewMode = LibraryViewMode.GRID), onEvent = {})
        }
    }
}
