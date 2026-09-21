package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.CollectionDownloadState
import androidx.compose.runtime.Composable
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
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
import kotlin.time.Duration.Companion.seconds

/**
 * The songs and, beside them, the player or the album they opened, the way `ListDetailScene` lays them out on a
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
        downloadStatuses = emptyMap(),
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
        download = CollectionDownloadState.NotDownloaded,
        downloadStatuses = emptyMap(),
    )

    private val player = PlayerUiState.Loaded(
        song = getLucky,
        status = PlaybackStatus.Playing,
        position = 18.seconds,
        duration = 29.seconds,
        repeatMode = RepeatMode.Off,
        isShuffleEnabled = false,
        hasPrevious = true,
        hasNext = true,
    )

    @Test
    fun recentlyPlayedBesidePlayer() {
        snapshot(name = "recently_played_beside_player", variants = ScreenshotVariant.all, isLandscape = true) {
            ListDetailScaffold(
                listPane = { RecentlyPlayedList() },
                detailPane = {
                    PlayerContent(
                        uiState = player,
                        layout = PlayerLayout.Stacked,
                        hasBack = false,
                        onEvent = {},
                    )
                },
            )
        }
    }

    @Test
    fun recentlyPlayedBesideNothingPlaying() {
        snapshot(name = "recently_played_beside_nothing_playing", isLandscape = true) {
            ListDetailScaffold(
                listPane = { RecentlyPlayedList() },
                detailPane = {
                    PlayerContent(
                        uiState = PlayerUiState.NothingPlaying,
                        layout = PlayerLayout.Stacked,
                        hasBack = false,
                        onEvent = {},
                    )
                },
            )
        }
    }

    @Test
    fun recentlyPlayedBesideAlbum() {
        snapshot(name = "recently_played_beside_album", isLandscape = true) {
            ListDetailScaffold(
                listPane = { RecentlyPlayedList() },
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
                            searchSongs.map { song ->
                                SearchResultUiModel(song = song, isUnavailable = false, downloadStatus = null)
                            },
                        ),
                        isHeaderInline = true,
                        onEvent = {},
                    )
                },
                detailPane = { AlbumContent(uiState = album, isHeaderInline = true, onEvent = {}) },
            )
        }
    }

    @Composable
    private fun RecentlyPlayedList() {
        SongsContent(
            uiState = songs,
            searchResults = emptyPagingItems(),
            isHeaderInline = true,
            onEvent = {},
        )
    }
}
