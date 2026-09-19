package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistLocalDataSource {
    fun observeAll(): Flow<List<Playlist>>

    fun observe(playlistId: Long): Flow<Playlist?>

    fun observeSongs(playlistId: Long): Flow<List<Song>>

    fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean>

    suspend fun create(name: String): Long

    suspend fun rename(
        playlistId: Long,
        name: String,
    )

    suspend fun delete(playlistId: Long)

    suspend fun addSong(
        playlistId: Long,
        song: Song,
    )

    suspend fun removeSong(
        playlistId: Long,
        songId: Long,
    )
}
