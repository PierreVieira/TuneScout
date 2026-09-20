package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.ObserveIsOnline
import kotlinx.coroutines.flow.Flow

internal class ObserveIsOnlineUseCase(
    private val repository: AlbumRepository,
) : ObserveIsOnline {
    override fun invoke(): Flow<Boolean> = repository.observeIsOnline()
}
