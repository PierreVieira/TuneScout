package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.RemoveFavorite

class RemoveFavoriteUseCase(
    private val repository: LibraryRepository,
) : RemoveFavorite {
    override suspend fun invoke(songId: Long) {
        repository.removeFavorite(songId)
    }
}
