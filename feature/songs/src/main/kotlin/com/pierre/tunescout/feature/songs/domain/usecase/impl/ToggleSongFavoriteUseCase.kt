package com.pierre.tunescout.feature.songs.domain.usecase.impl

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.domain.usecase.ToggleSongFavorite

internal class ToggleSongFavoriteUseCase(
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
) : ToggleSongFavorite {
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
