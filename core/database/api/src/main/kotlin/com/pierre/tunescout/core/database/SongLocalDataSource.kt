package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface SongLocalDataSource {
    suspend fun save(songs: List<Song>)

    fun observe(songId: Long): Flow<Song?>

    suspend fun find(songId: Long): Song?
}
