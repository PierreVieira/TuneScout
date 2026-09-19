package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedLocalDataSource {
    fun observe(limit: Int): Flow<List<Song>>

    suspend fun record(song: Song)

    suspend fun remove(songId: Long)
}
