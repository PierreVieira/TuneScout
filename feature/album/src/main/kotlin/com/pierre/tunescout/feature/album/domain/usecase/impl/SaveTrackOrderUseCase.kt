package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.SaveTrackOrder

internal class SaveTrackOrderUseCase(
    private val repository: AlbumRepository,
) : SaveTrackOrder {
    override suspend fun invoke(
        albumId: Long,
        songIds: List<Long>,
    ) {
        repository.saveTrackOrder(albumId = albumId, songIds = songIds)
    }
}
