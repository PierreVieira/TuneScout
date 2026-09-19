package com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.ObserveSong
import kotlinx.coroutines.flow.Flow

internal class ObserveSongUseCase(
    private val repository: AddToPlaylistRepository,
) : ObserveSong {
    override fun invoke(songId: Long): Flow<Song?> = repository.observeSong(songId)
}
