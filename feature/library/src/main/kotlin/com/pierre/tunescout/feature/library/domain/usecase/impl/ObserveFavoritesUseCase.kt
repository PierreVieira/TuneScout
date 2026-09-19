package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObserveFavorites
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: LibraryRepository,
) : ObserveFavorites {
    override fun invoke(): Flow<List<Song>> = repository.observeFavorites()
}
