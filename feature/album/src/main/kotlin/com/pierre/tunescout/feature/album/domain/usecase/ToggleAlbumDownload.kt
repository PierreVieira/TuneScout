package com.pierre.tunescout.feature.album.domain.usecase

import com.pierre.tunescout.core.model.Album

fun interface ToggleAlbumDownload {
    /**
     * @param isDownloaded whether the album is downloaded now, which is what the toggle takes back.
     * @param isFavorite whether the album is in the library now.
     */
    suspend operator fun invoke(
        album: Album,
        isDownloaded: Boolean,
        isFavorite: Boolean,
    )
}
