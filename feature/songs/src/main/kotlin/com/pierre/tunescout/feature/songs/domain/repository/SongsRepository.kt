package com.pierre.tunescout.feature.songs.domain.repository

import androidx.paging.PagingData
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface SongsRepository {
    fun searchSongs(term: String): Flow<PagingData<Song>>

    fun observeRecentlyPlayed(): Flow<List<Song>>

    suspend fun removeFromRecentlyPlayed(songId: Long)
}
