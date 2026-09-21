package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.core.model.NowPlaying
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
        songPendingRemoval = null,
        unplayableSongIds = emptySet(),
        isPlaying = true,
        isShuffleEnabled = false,
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

    @Test
    fun confirmingRemoval() {
        snapshot(name = "confirming_removal") {
            CollectionContent(uiState = loaded.copy(songPendingRemoval = getLucky), onEvent = {})
        }
    }
}
