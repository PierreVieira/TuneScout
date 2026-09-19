package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import kotlinx.coroutines.flow.Flow

interface FavoriteAlbumLocalDataSource {
    fun observeAll(): Flow<List<AlbumSummary>>

    fun observeIsFavorite(albumId: Long): Flow<Boolean>

    suspend fun add(album: Album)

    suspend fun remove(albumId: Long)
}
