package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.NowPlaying
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

    @Test
    fun loadedPlayingAndNotFavorite() {
        snapshot(name = "loaded_playing") {
            AlbumContent(
                uiState = loaded.copy(
                    nowPlaying = NowPlaying(songId = getLucky.id, isPlaying = true),
                    isFavorite = false,
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
}
