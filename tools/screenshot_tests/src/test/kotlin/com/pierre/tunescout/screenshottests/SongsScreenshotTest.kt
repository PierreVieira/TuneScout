package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.pagingItems
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.searchSongs
import org.junit.Test

internal class SongsScreenshotTest : ScreenshotTest() {
    private val recent = SongsUiState(
        query = "",
        recentlyPlayed = recentlyPlayed,
        nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
        songPendingRemoval = null,
        isOffline = false,
    )

    @Test
    fun recentlyPlayed() {
        snapshot(name = "recently_played", variants = ScreenshotVariant.all) {
            SongsContent(uiState = recent, searchResults = emptyPagingItems(), isHeaderInline = false, onEvent = {})
        }
    }

    @Test
    fun empty() {
        snapshot(name = "empty") {
            SongsContent(
                uiState = recent.copy(recentlyPlayed = emptyList(), nowPlaying = null),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun searching() {
        snapshot(name = "searching") {
            SongsContent(
                uiState = recent.copy(query = "daft punk"),
                searchResults = pagingItems(searchSongs),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun searchWithoutResults() {
        snapshot(name = "search_without_results") {
            SongsContent(
                uiState = recent.copy(query = "nothing like this"),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun offline() {
        snapshot(name = "offline") {
            SongsContent(
                uiState = recent.copy(isOffline = true),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun confirmingRemoval() {
        snapshot(name = "confirming_removal") {
            SongsContent(
                uiState = recent.copy(songPendingRemoval = getLucky),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    /** The header sits beside the list once the window is wide enough for it. */
    @Test
    fun inlineHeader() {
        snapshot(name = "inline_header", isLandscape = true) {
            SongsContent(uiState = recent, searchResults = emptyPagingItems(), isHeaderInline = true, onEvent = {})
        }
    }
}
