package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ReorderPlaylistSongs

class ReorderPlaylistSongsUseCase(
    private val repository: LibraryRepository,
) : ReorderPlaylistSongs {
    override suspend fun invoke(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        repository.reorderPlaylistSongs(playlistId = playlistId, songIds = songIds)
    }
}
