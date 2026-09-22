package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import org.junit.Test

internal class AlbumScreenshotTest : ScreenshotTest() {
    private val loaded = AlbumUiState.Loaded(
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
        songAlreadyQueued = null,
    )

    @Test
    fun loading() {
        snapshot(name = "loading") {
            AlbumContent(uiState = AlbumUiState.Loading, isHeaderInline = false, onEvent = {})
        }
    }

    @Test
    fun error() {
        snapshot(name = "error") {
            AlbumContent(uiState = AlbumUiState.Error, isHeaderInline = false, onEvent = {})
        }
    }

    @Test
    fun loaded() {
        snapshot(name = "loaded", variants = ScreenshotVariant.all) {
            AlbumContent(uiState = loaded, isHeaderInline = false, onEvent = {})
        }
    }

    /** A downloaded album: the switch is on, and every track wears the accent arrow. */
    @Test
    fun downloaded() {
        snapshot(name = "downloaded", variants = ScreenshotVariant.all) {
            AlbumContent(
                uiState = loaded.copy(
                    download = CollectionDownloadState.Downloaded,
                    downloadStatuses = randomAccessMemories.songs.associate { song ->
                        song.id to SongDownloadStatus.Downloaded
                    },
                ),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun loadedPlayingAndNotFavorite() {
        snapshot(name = "loaded_playing") {
            AlbumContent(
                uiState = loaded.copy(
                    nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
                    isFavorite = false,
                    isPlaying = true,
                    isShuffleEnabled = true,
                ),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    /** Offline: the tracks the player cannot reach are dimmer, so a refused tap is seen coming. */
    @Test
    fun loadedWithUnplayableTracks() {
        snapshot(name = "loaded_unplayable") {
            AlbumContent(
                uiState = loaded.copy(
                    unplayableSongIds = randomAccessMemories.songs
                        .drop(2)
                        .map { song -> song.id }
                        .toSet(),
                ),
                isHeaderInline = false,
                onEvent = {},
            )
        }
    }

    @Test
    fun loadedStale() {
        snapshot(name = "loaded_stale") {
            AlbumContent(uiState = loaded.copy(isStale = true), isHeaderInline = false, onEvent = {})
        }
    }

    @Test
    fun loadedInlineHeader() {
        snapshot(name = "loaded_inline_header") {
            AlbumContent(uiState = loaded, isHeaderInline = true, onEvent = {})
        }
    }

    /** Reordering: a handle on every track instead of its options, and done in place of the overflow. */
    @Test
    fun reordering() {
        snapshot(name = "reordering", variants = ScreenshotVariant.all) {
            AlbumContent(uiState = loaded.copy(isReordering = true), isHeaderInline = false, onEvent = {})
        }
    }
}
