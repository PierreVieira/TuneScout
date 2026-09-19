package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObserveFavoriteAlbums
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteAlbumsUseCase(
    private val repository: LibraryRepository,
) : ObserveFavoriteAlbums {
    override fun invoke(): Flow<List<AlbumSummary>> = repository.observeFavoriteAlbums()
}
