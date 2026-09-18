package com.quare.tunescout.core.database

import com.quare.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumLocalDataSource {
    suspend fun save(album: Album)

    fun observe(albumId: Long): Flow<Album?>
}
