package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface SongLocalDataSource {
    suspend fun save(songs: List<Song>)

    fun observe(songId: Long): Flow<Song?>

    suspend fun find(songId: Long): Song?

    /**
     * What a search can still answer with when the API cannot be reached.
     *
     * @return at most [limit] songs already on the device whose title, artist or album contains
     * [term], most recently cached first.
     */
    suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<Song>
}
