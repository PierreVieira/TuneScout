package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.feature.album.domain.usecase.IsAlbumFavorite
import kotlinx.coroutines.flow.Flow

internal class IsAlbumFavoriteUseCase(
    private val favoriteAlbumLocalDataSource: FavoriteAlbumLocalDataSource,
) : IsAlbumFavorite {
    override fun invoke(albumId: Long): Flow<Boolean> = favoriteAlbumLocalDataSource.observeIsFavorite(albumId)
}
