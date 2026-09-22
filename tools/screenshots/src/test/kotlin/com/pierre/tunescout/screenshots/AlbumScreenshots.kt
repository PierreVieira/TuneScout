package com.pierre.tunescout.screenshots

import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.feature.album.presentation.content.AlbumContent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.screenshotfixtures.randomAccessMemories
import org.junit.Test

internal class AlbumScreenshots : ReadmeScreenshotsTest() {
    private val loadedAlbum = AlbumUiState.Loaded(
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
