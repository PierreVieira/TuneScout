package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.CreatePlaylist

class CreatePlaylistUseCase(
    private val repository: LibraryRepository,
) : CreatePlaylist {
    override suspend fun invoke(name: String): Long = repository.createPlaylist(name)
}
