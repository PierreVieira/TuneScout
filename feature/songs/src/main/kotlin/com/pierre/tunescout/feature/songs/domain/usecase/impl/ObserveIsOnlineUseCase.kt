package com.pierre.tunescout.feature.songs.domain.usecase.impl

import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveIsOnline
import kotlinx.coroutines.flow.Flow

internal class ObserveIsOnlineUseCase(
    private val repository: SongsRepository,
) : ObserveIsOnline {
    override fun invoke(): Flow<Boolean> = repository.observeIsOnline()
}
