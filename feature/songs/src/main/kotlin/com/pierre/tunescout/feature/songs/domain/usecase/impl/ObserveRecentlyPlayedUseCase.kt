package com.pierre.tunescout.feature.songs.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import kotlinx.coroutines.flow.Flow

internal class ObserveRecentlyPlayedUseCase(
    private val repository: SongsRepository,
) : ObserveRecentlyPlayed {
    override fun invoke(): Flow<List<Song>> = repository.observeRecentlyPlayed()
}
