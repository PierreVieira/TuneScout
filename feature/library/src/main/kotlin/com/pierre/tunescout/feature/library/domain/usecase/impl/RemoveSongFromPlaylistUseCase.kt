package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.RemoveSongFromPlaylist

class RemoveSongFromPlaylistUseCase(
    private val repository: LibraryRepository,
) : RemoveSongFromPlaylist {
    override suspend fun invoke(
        playlistId: Long,
        songId: Long,
    ) {
        repository.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
    }
}
