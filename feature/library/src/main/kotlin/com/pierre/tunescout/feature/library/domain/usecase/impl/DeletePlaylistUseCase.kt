package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.DeletePlaylist

class DeletePlaylistUseCase(
    private val repository: LibraryRepository,
) : DeletePlaylist {
    override suspend fun invoke(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}
