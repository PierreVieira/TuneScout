package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.feature.library.presentation.content.CollectionContent
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import org.junit.Test

internal class CollectionScreenshotTest : ScreenshotTest() {
    private val loaded = CollectionUiState.Loaded(
        title = CollectionTitle.Custom("Late night drive"),
        songs = recentlyPlayed,
        nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
        isDeletable = true,
        favoriteSongIds = emptySet(),
        unplayableSongIds = emptySet(),
        isPlaying = true,
        isShuffleEnabled = false,
        isReorderable = true,
        isReordering = false,
        download = CollectionDownloadState.NotDownloaded,
        downloadStatuses = emptyMap(),
    )

    @Test
    fun loading() {
        snapshot(name = "loading") {
            CollectionContent(uiState = CollectionUiState.Loading, onEvent = {})
        }
    }

    @Test
    fun loaded() {
        snapshot(name = "loaded") {
            CollectionContent(uiState = loaded, onEvent = {})
        }
    }

    /** The favourites list is named by a string resource and cannot be deleted. */
    @Test
    fun favorites() {
        snapshot(name = "favorites", variants = ScreenshotVariant.all) {
            CollectionContent(
                uiState = loaded.copy(
                    title = CollectionTitle.Favorites,
                    isDeletable = false,
                    nowPlaying = null,
                    isPlaying = false,
                    isShuffleEnabled = true,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun empty() {
        snapshot(name = "empty") {
            CollectionContent(
                uiState = loaded.copy(songs = emptyList(), nowPlaying = null, isPlaying = false),
                onEvent = {},
            )
        }
    }

    /** Downloading the playlist: the switch fills up, and each song says how far it has got. */
    @Test
    fun downloading() {
        snapshot(name = "downloading", variants = ScreenshotVariant.all) {
            val first = recentlyPlayed.first().id
            CollectionContent(
                uiState = loaded.copy(
                    download = CollectionDownloadState.Downloading(
                        downloadedCount = 1,
                        totalCount = recentlyPlayed.size,
                    ),
                    downloadStatuses = recentlyPlayed.associate { song ->
                        song.id to
                            if (song.id == first) SongDownloadStatus.Downloaded else SongDownloadStatus.Downloading
                    },
                ),
                onEvent = {},
            )
        }
    }

    /** Reordering: a handle on every song instead of its options, and done in place of the overflow. */
    @Test
    fun reordering() {
        snapshot(name = "reordering", variants = ScreenshotVariant.all) {
            CollectionContent(uiState = loaded.copy(isReordering = true), onEvent = {})
        }
    }
}
