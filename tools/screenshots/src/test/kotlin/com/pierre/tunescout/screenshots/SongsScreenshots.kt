package com.pierre.tunescout.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerContent
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.pagingItems
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.searchSongs
import com.pierre.tunescout.ui.component.PlayButtonState
import org.junit.Test

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
                        isHeaderInline = false,
                        uiState = SongsUiState(
                            query = "",
                            isAudioSearchAvailable = true,
                            recentlyPlayed = recentlyPlayed,
                            nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = false),
                            songPendingRemoval = null,
                            isOffline = false,
                            unplayableSongIds = emptySet(),
                        ),
                        searchResults = emptyPagingItems(),
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
                isHeaderInline = false,
                uiState = SongsUiState(
                    query = SEARCH_QUERY,
                    isAudioSearchAvailable = true,
                    recentlyPlayed = recentlyPlayed,
                    nowPlaying = null,
                    songPendingRemoval = null,
                    isOffline = false,
                    unplayableSongIds = emptySet(),
                ),
                searchResults = pagingItems(
                    searchSongs.map { song ->
                        SearchResultUiModel(song = song, isUnavailable = false)
                    },
                ),
                onEvent = {},
            )
        }
    }

    private companion object {
        const val SEARCH_QUERY = "daft punk"
        const val RESTORED_PROGRESS = 0.62f
    }
}
