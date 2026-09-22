package com.pierre.tunescout.feature.player.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.player.domain.usecase.ToggleFavorite

internal class ToggleFavoriteUseCase(
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
) : ToggleFavorite {
    override suspend fun invoke(
        song: Song,
        isFavorite: Boolean,
    ) {
        if (isFavorite) {
            favoriteSongLocalDataSource.remove(song.id)
        } else {
            favoriteSongLocalDataSource.add(song)
        }
    }
}
