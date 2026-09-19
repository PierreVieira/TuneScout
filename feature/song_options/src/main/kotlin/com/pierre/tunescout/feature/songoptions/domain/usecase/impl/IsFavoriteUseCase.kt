package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.IsFavorite
import kotlinx.coroutines.flow.Flow

internal class IsFavoriteUseCase(
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
) : IsFavorite {
    override fun invoke(songId: Long): Flow<Boolean> = favoriteSongLocalDataSource.observeIsFavorite(songId)
}
