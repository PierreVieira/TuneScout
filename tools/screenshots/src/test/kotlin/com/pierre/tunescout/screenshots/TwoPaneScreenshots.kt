package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.content.LibraryContent
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.giorgioByMoroder
import com.pierre.tunescout.screenshotfixtures.harderBetterFasterStronger
import com.pierre.tunescout.screenshotfixtures.instantCrush
import com.pierre.tunescout.screenshotfixtures.oneMoreTime
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.screenshotfixtures.touch
import com.pierre.tunescout.ui.component.ListDetailScaffold
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Landscape tablet shots for the README: past the app's 800dp two-pane breakpoint, a list keeps
 * what it opens beside it instead of losing it under a full-screen detail. See
 * [ReadmeTabletScreenshotsTest] for why these can't go through `capture()`.
 */
internal class TwoPaneScreenshots : ReadmeTabletScreenshotsTest() {
    @Test
    fun songsBesidePlayer() {
        captureTwoPane(
            fileName = "two_pane_songs",
            title = "One breakpoint, one more pane",
            description = "At 800dp wide, opening a song keeps the list on screen instead of covering it",
        ) {
            ListDetailScaffold(
                listPane = {
                    SongsContent(
                        isHeaderInline = true,
                        uiState = SongsUiState(
                            query = "",
                            isAudioSearchAvailable = true,
                            recentlyPlayed = recentlyPlayed,
                            nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
                            songPendingRemoval = null,
                            favoriteSongIds = emptySet(),
                            isOffline = false,
                            unplayableSongIds = emptySet(),
                            downloadStatuses = emptyMap(),
                        ),
                        searchResults = emptyPagingItems(),
                        onEvent = {},
                    )
                },
                detailPane = {
                    PlayerContent(
                        layout = PlayerLayout.Stacked,
                        uiState = PlayerUiState.Loaded(
                            song = getLucky,
                            status = PlaybackStatus.Playing,
                            position = 18.seconds,
                            duration = 29.seconds,
                            repeatMode = RepeatMode.Off,
                            isShuffleEnabled = false,
                            hasPrevious = true,
                            hasNext = true,
                        ),
                        onEvent = {},
                    )
                },
            )
        }
    }

    @Test
    fun libraryBesideAlbum() {
        captureTwoPane(
            fileName = "two_pane_library",
            title = "The same pane, every detail",
            description = "The album, the player, the queue: every detail opens beside its list once there's room",
        ) {
            ListDetailScaffold(
                listPane = {
                    LibraryContent(
                        uiState = LibraryUiState(
                            items = listOf(
                                LibraryItemUiModel.Favorites(
                                    songCount = recentlyPlayed.size,
                                    artworks = recentlyPlayed.map { song -> song.artwork },
                                ),
                                LibraryItemUiModel.Playlist(
                                    id = 1,
                                    name = "Late night drive",
                                    songCount = 4,
                                    artworks = listOf(getLucky, instantCrush, touch, giorgioByMoroder)
                                        .map { song -> song.artwork },
                                ),
                                LibraryItemUiModel.Playlist(
                                    id = 2,
                                    name = "Discovery on repeat",
                                    songCount = 2,
                                    artworks = listOf(oneMoreTime, harderBetterFasterStronger)
                                        .map { song -> song.artwork },
                                ),
                                LibraryItemUiModel.Album(
                                    id = randomAccessMemories.id,
                                    title = randomAccessMemories.title,
                                    artistName = randomAccessMemories.artistName,
                                    artwork = randomAccessMemories.artwork,
                                ),
                            ),
                            viewMode = LibraryViewMode.LIST,
                            filters = emptySet(),
                            downloadedKeys = emptySet(),
                        ),
                        isTwoPane = true,
                        onEvent = {},
                    )
                },
                detailPane = {
                    AlbumContent(
                        isHeaderInline = true,
                        uiState = AlbumUiState.Loaded(
                            album = randomAccessMemories,
                            nowPlaying = null,
                            isFavorite = true,
                            isStale = false,
                            favoriteSongIds = emptySet(),
                            unplayableSongIds = emptySet(),
                            isPlaying = false,
                            isShuffleEnabled = false,
                            isReordering = false,
                            download = CollectionDownloadState.NotDownloaded,
                            downloadStatuses = emptyMap(),
                        ),
                        onEvent = {},
                    )
                },
            )
        }
    }
}
