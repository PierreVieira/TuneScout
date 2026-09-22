package com.pierre.tunescout.screenshottests

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.pagingItems
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.searchSongs
import org.junit.Test

internal class SongsScreenshotTest : ScreenshotTest() {
    private val recent = SongsUiState(
        query = "",
        isAudioSearchAvailable = true,
        recentlyPlayed = recentlyPlayed,
        nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
        songPendingRemoval = null,
        favoriteSongIds = emptySet(),
        isOffline = false,
        unplayableSongIds = emptySet(),
        downloadStatuses = emptyMap(),
        songAlreadyQueued = null,
    )

    @Test
    fun recentlyPlayed() {
        snapshot(name = "recently_played", variants = ScreenshotVariant.all) {
            SongsContent(uiState = recent, searchResults = emptyPagingItems(), isHeaderInline = false, onEvent = {})
        }
    }

    /** A device with no recognition service gets the field alone, back at its full width. */
    @Test
    fun withoutAudioSearch() {
        snapshot(name = "without_audio_search") {
            SongsContent(
                uiState = recent.copy(isAudioSearchAvailable = false),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
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
                searchResults = pagingItems(searchResults(searchSongs)),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    /** The ten search songs fit on the screen, which leaves nothing to scroll. */
    private val longerThanTheScreen = (searchSongs + randomAccessMemories.songs).distinctBy { song -> song.id }

    /** Scrolled away from the top, the header casts a shadow over the rows passing under it. */
    @Test
    fun searchingScrolled() {
        snapshot(
            name = "searching_scrolled",
            beforeCapture = { onNode(hasScrollToIndexAction()).performScrollToIndex(SCROLLED_INDEX) },
        ) {
            SongsContent(
                uiState = recent.copy(query = "daft punk"),
                searchResults = pagingItems(searchResults(longerThanTheScreen)),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    /**
     * A drag past the header's own height hides it completely. Nothing should be left drawn where
     * it used to be: the shadow it casts while scrolled degenerates once the header has no height
     * of its own to cast one from, and used to paint the gap solid black instead of leaving it
     * empty.
     */
    @Test
    fun searchingHeaderHidden() {
        snapshot(
            name = "searching_header_hidden",
            beforeCapture = {
                onNode(hasScrollToIndexAction()).performTouchInput {
                    swipeUp(startY = visibleSize.height * 0.8f, endY = visibleSize.height * 0.2f)
                }
            },
        ) {
            SongsContent(
                uiState = recent.copy(query = "daft punk"),
                searchResults = pagingItems(searchResults(longerThanTheScreen)),
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

    /** Offline: the rows the player cannot reach are dimmer, so a tap that is refused is seen coming. */
    @Test
    fun offline() {
        snapshot(name = "offline") {
            SongsContent(
                uiState = recent.copy(
                    isOffline = true,
                    unplayableSongIds = recentlyPlayed.drop(1).map { song -> song.id }.toSet(),
                ),
                searchResults = emptyPagingItems(),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun searchingWithUnavailableResults() {
        snapshot(name = "searching_with_unavailable_results") {
            SongsContent(
                uiState = recent.copy(query = "daft punk", isOffline = true),
                searchResults = pagingItems(
                    searchSongs.mapIndexed { index, song ->
                        SearchResultUiModel(song = song, isUnavailable = index % 2 == 1, downloadStatus = null)
                    },
                ),
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

    private fun searchResults(songs: List<Song>): List<SearchResultUiModel> =
        songs.map { song -> SearchResultUiModel(song = song, isUnavailable = false, downloadStatus = null) }

    /** The header sits beside the list once the window is wide enough for it. */
    @Test
    fun inlineHeader() {
        snapshot(name = "inline_header", isLandscape = true) {
            SongsContent(uiState = recent, searchResults = emptyPagingItems(), isHeaderInline = true, onEvent = {})
        }
    }

    private companion object {
        const val SCROLLED_INDEX = 3
    }
}
