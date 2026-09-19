package com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.ObservePlaylists
import kotlinx.coroutines.flow.Flow

internal class ObservePlaylistsUseCase(
    private val repository: AddToPlaylistRepository,
) : ObservePlaylists {
    override fun invoke(): Flow<List<Playlist>> = repository.observePlaylists()
}
