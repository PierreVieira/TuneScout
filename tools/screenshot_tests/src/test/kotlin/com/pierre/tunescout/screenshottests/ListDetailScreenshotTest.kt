package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.pagingItems
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.searchSongs
import com.pierre.tunescout.ui.component.ListDetailScaffold
import org.junit.Test

/**
 * The songs and the album they opened, side by side, the way `ListDetailScene` lays them out on a
 * window wide enough for two panes. The navigation rail is drawn outside the scene, so it is not here.
 */
internal class ListDetailScreenshotTest : ScreenshotTest() {
    private val songs = SongsUiState(
        query = "",
        isAudioSearchAvailable = true,
        recentlyPlayed = recentlyPlayed,
        nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
        songPendingRemoval = null,
        favoriteSongIds = emptySet(),
        isOffline = false,
        unplayableSongIds = emptySet(),
    )

    private val album = AlbumUiState.Loaded(
        album = randomAccessMemories,
        nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
        isFavorite = true,
        isStale = false,
        favoriteSongIds = emptySet(),
        unplayableSongIds = emptySet(),
        isPlaying = true,
        isShuffleEnabled = false,
        isReordering = false,
    )

    @Test
    fun recentlyPlayedBesideAlbum() {
        snapshot(name = "recently_played_beside_album", isLandscape = true) {
            ListDetailScaffold(
                listPane = {
                    SongsContent(
                        uiState = songs,
                        searchResults = emptyPagingItems(),
                        isHeaderInline = true,
                        onEvent = {},
                    )
                },
                detailPane = { AlbumContent(uiState = album, isHeaderInline = true, onEvent = {}) },
            )
        }
    }

    /** The pair that reads longest: Portuguese at a large font, in half the width each. */
    @Test
    fun searchResultsBesideAlbum() {
        snapshot(name = "search_results_beside_album", variants = ScreenshotVariant.all, isLandscape = true) {
            ListDetailScaffold(
                listPane = {
                    SongsContent(
                        uiState = songs.copy(query = "daft punk"),
                        searchResults = pagingItems(
                            searchSongs.map { song -> SearchResultUiModel(song = song, isUnavailable = false) },
                        ),
                        isHeaderInline = true,
                        onEvent = {},
                    )
                },
                detailPane = { AlbumContent(uiState = album, isHeaderInline = true, onEvent = {}) },
            )
        }
    }
}
