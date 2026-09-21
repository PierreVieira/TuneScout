package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.feature.album.domain.usecase.ObserveFavoriteSongIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveFavoriteSongIdsUseCase(
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
) : ObserveFavoriteSongIds {
    override fun invoke(): Flow<Set<Long>> = favoriteSongLocalDataSource
        .observeAll()
        .map { songs -> songs.mapTo(mutableSetOf()) { song -> song.id } }
}
