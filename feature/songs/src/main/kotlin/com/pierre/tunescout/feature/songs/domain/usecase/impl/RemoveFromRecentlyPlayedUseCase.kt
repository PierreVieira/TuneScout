package com.pierre.tunescout.feature.songs.domain.usecase.impl

import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.RemoveFromRecentlyPlayed

internal class RemoveFromRecentlyPlayedUseCase(
    private val repository: SongsRepository,
) : RemoveFromRecentlyPlayed {
    override suspend fun invoke(songId: Long) {
        repository.removeFromRecentlyPlayed(songId)
    }
}
