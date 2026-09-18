package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.RefreshAlbum

internal class RefreshAlbumUseCase(
    private val repository: AlbumRepository,
) : RefreshAlbum {
    override suspend fun invoke(albumId: Long): Result<Unit> = repository.refreshAlbum(albumId)
}
