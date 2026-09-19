package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylistSongs
import kotlinx.coroutines.flow.Flow

class ObservePlaylistSongsUseCase(
    private val repository: LibraryRepository,
) : ObservePlaylistSongs {
    override fun invoke(playlistId: Long): Flow<List<Song>> = repository.observePlaylistSongs(playlistId)
}
