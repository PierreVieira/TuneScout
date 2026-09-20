package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumLocalDataSource {
    suspend fun save(album: Album)

    fun observe(albumId: Long): Flow<Album?>
}
