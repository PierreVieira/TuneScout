package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.album.domain.usecase.ToggleAlbumDownload

/**
 * Downloading an album also puts it in the library, like Spotify does: offline, the library is
 * where it is found again. Taking the download back leaves it there. An album put together from the
 * saved tracks is downloaded but not liked, since liking would store it as if it were whole.
 *
 * @property downloadLocalDataSource where the request is kept.
 * @property favoriteAlbumLocalDataSource the library the album joins.
 */
internal class ToggleAlbumDownloadUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
    private val favoriteAlbumLocalDataSource: FavoriteAlbumLocalDataSource,
) : ToggleAlbumDownload {
    override suspend fun invoke(
        album: Album,
        isDownloaded: Boolean,
        isFavorite: Boolean,
    ) {
        val key = LibraryItemKey.Album(albumId = album.id)
        if (isDownloaded) return downloadLocalDataSource.removeCollection(key)
        downloadLocalDataSource.addCollection(key)
        if (album.isComplete && !isFavorite) favoriteAlbumLocalDataSource.add(album)
    }
}
