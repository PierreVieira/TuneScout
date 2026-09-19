package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylist
import kotlinx.coroutines.flow.Flow

class ObservePlaylistUseCase(
    private val repository: LibraryRepository,
) : ObservePlaylist {
    override fun invoke(playlistId: Long): Flow<Playlist?> = repository.observePlaylist(playlistId)
}
