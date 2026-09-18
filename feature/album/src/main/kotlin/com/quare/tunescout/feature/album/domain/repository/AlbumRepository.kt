package com.quare.tunescout.feature.album.domain.repository

import com.quare.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbum(albumId: Long): Flow<Album?>

    suspend fun refreshAlbum(albumId: Long): Result<Unit>
}
