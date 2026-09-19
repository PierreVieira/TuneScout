package com.pierre.tunescout.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerContent
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.component.PlayButtonState
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

private const val SEARCH_QUERY = "daft punk"
private const val RESTORED_PROGRESS = 0.62f

internal class SongsScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun songs() {
        capture(
            fileName = "songs",
            title = "Pick up where you left off",
            description = "Recently played is the home screen, and the last song waits where you paused it",
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    SongsContent(
                        uiState = SongsUiState(
                            query = "",
                            recentlyPlayed = recentlyPlayed,
                            nowPlayingId = getLucky.id,
                        ),
                        searchResults = pagingItems(emptyList()),
                        onEvent = {},
                    )
                }
                MiniPlayerContent(
                    song = getLucky,
                    playButtonState = PlayButtonState.Play,
                    progress = RESTORED_PROGRESS,
                    onEvent = {},
                )
            }
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
