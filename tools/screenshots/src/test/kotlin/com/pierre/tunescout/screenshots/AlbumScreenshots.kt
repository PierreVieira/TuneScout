package com.pierre.tunescout.screenshots

import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import org.junit.Test

internal class AlbumScreenshots : ReadmeScreenshotsTest() {
    private val loadedAlbum = AlbumUiState.Loaded(
        album = randomAccessMemories,
        nowPlaying = null,
        isFavorite = true,
        isStale = false,
        unplayableSongIds = emptySet(),
    )

    @Test
    fun album() {
        capture(
            fileName = "album",
            title = "The album behind the song",
            description = "Fetched once and cached, so it opens again without a connection",
        ) {
            AlbumContent(isHeaderInline = false, uiState = loadedAlbum, onEvent = {})
        }
    }
}
