package com.pierre.tunescout.screenshottests

import androidx.compose.runtime.Composable
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.feature.addtoplaylist.presentation.content.AddToPlaylistContent
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.screenshotfixtures.aroundTheWorld
import com.pierre.tunescout.screenshotfixtures.daFunk
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.screenshotfixtures.instantCrush
import com.pierre.tunescout.screenshotfixtures.oneMoreTime
import org.junit.Test

internal class AddToPlaylistScreenshotTest : ScreenshotTest() {
    /** One playlist per cover: the quadrant grid, a single song's artwork, and the empty placeholder. */
    private val playlists = listOf(
        Playlist(
            id = 1,
            name = "Late night drive",
            songCount = 4,
            artworks = listOf(oneMoreTime, daFunk, getLucky, aroundTheWorld).map { song -> song.artwork },
        ),
        Playlist(id = 2, name = "Random Access Memories", songCount = 1, artworks = listOf(instantCrush.artwork)),
        Playlist(id = 3, name = "To listen later", songCount = 0, artworks = emptyList()),
    )

    @Test
    fun playlists() {
        snapshot(name = "playlists", variants = ScreenshotVariant.all) {
            AddToPlaylistSheet(AddToPlaylistUiState(song = getLucky, playlists = playlists, newPlaylistName = null))
        }
    }

    @Test
    fun noPlaylists() {
        snapshot(name = "no_playlists") {
            AddToPlaylistSheet(AddToPlaylistUiState(song = getLucky, playlists = emptyList(), newPlaylistName = null))
        }
    }

    @Composable
    private fun AddToPlaylistSheet(uiState: AddToPlaylistUiState) {
        SheetOverScreen(
            screen = {},
            sheet = { AddToPlaylistContent(uiState = uiState, onEvent = {}) },
        )
    }
}
