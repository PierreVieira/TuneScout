package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylists
import kotlinx.coroutines.flow.Flow

class ObservePlaylistsUseCase(
    private val repository: LibraryRepository,
) : ObservePlaylists {
    override fun invoke(): Flow<List<Playlist>> = repository.observePlaylists()
}
