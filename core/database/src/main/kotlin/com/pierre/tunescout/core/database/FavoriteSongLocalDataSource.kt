package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface FavoriteSongLocalDataSource {
    fun observeAll(): Flow<List<Song>>

    fun observeIsFavorite(songId: Long): Flow<Boolean>

    suspend fun add(song: Song)

    suspend fun remove(songId: Long)
}
