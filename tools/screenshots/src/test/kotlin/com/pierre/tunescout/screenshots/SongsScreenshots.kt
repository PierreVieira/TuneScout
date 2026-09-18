package com.pierre.tunescout.screenshots

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

private const val SEARCH_QUERY = "daft punk"

internal class SongsScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun songs() {
        capture(
            fileName = "songs",
            title = "Pick up where you left off",
            description = "Recently played is the home screen, kept on device and ready offline",
        ) {
            SongsContent(
                uiState = SongsUiState(query = "", recentlyPlayed = recentlyPlayed, nowPlayingId = null),
                searchResults = pagingItems(emptyList()),
                onEvent = {},
            )
        }
    }

    @Test
    fun search() {
        capture(
            fileName = "search",
            title = "Find any song as you type",
            description = "The iTunes catalog, debounced and paged as you scroll",
        ) {
            SongsContent(
                uiState = SongsUiState(query = SEARCH_QUERY, recentlyPlayed = recentlyPlayed, nowPlayingId = null),
                searchResults = pagingItems(searchSongs),
                onEvent = {},
            )
        }
    }
}

@Composable
private fun pagingItems(songs: List<Song>): LazyPagingItems<Song> =
    flowOf(PagingData.from(songs)).collectAsLazyPagingItems()
