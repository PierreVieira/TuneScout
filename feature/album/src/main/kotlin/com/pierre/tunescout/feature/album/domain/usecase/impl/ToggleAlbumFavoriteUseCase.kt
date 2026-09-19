package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.feature.album.domain.usecase.ToggleAlbumFavorite

internal class ToggleAlbumFavoriteUseCase(
    private val favoriteAlbumLocalDataSource: FavoriteAlbumLocalDataSource,
) : ToggleAlbumFavorite {
    override suspend fun invoke(
        album: Album,
        isFavorite: Boolean,
    ) {
        if (isFavorite) {
            favoriteAlbumLocalDataSource.remove(album.id)
        } else {
            favoriteAlbumLocalDataSource.add(album)
        }
    }
}
