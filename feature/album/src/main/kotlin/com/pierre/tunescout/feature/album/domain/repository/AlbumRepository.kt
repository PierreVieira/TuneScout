package com.pierre.tunescout.feature.album.domain.repository

import com.pierre.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbum(albumId: Long): Flow<Album?>

    suspend fun refreshAlbum(albumId: Long): Result<Unit>
}
