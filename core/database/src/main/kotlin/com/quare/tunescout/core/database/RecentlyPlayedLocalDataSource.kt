package com.quare.tunescout.core.database

import com.quare.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedLocalDataSource {
    fun observe(limit: Int): Flow<List<Song>>

    suspend fun record(song: Song)
}
