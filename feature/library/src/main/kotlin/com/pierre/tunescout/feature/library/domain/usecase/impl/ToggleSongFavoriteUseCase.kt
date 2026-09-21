package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ToggleSongFavorite

class ToggleSongFavoriteUseCase(
    private val repository: LibraryRepository,
) : ToggleSongFavorite {
    override suspend fun invoke(
        song: Song,
        isFavorite: Boolean,
    ) {
        if (isFavorite) {
            repository.removeFavorite(song.id)
        } else {
            repository.addFavorite(song)
        }
    }
}
