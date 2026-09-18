package com.quare.tunescout.feature.songs.domain.usecase.impl

import com.quare.tunescout.core.model.Song
import com.quare.tunescout.feature.songs.domain.repository.SongsRepository
import com.quare.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import kotlinx.coroutines.flow.Flow

internal class ObserveRecentlyPlayedUseCase(
    private val repository: SongsRepository,
) : ObserveRecentlyPlayed {
    override fun invoke(): Flow<List<Song>> = repository.observeRecentlyPlayed()
}
