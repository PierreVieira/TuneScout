package com.pierre.tunescout.feature.album.domain.usecase.impl

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import kotlinx.coroutines.flow.Flow

internal class ObserveAlbumUseCase(
    private val repository: AlbumRepository,
) : ObserveAlbum {
    override fun invoke(albumId: Long): Flow<Album?> = repository.observeAlbum(albumId)
}
