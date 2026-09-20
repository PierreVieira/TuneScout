package com.pierre.tunescout.core.network

import com.pierre.tunescout.core.model.Song

fun interface SongSearchRemoteDataSource {
    suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song>
}
