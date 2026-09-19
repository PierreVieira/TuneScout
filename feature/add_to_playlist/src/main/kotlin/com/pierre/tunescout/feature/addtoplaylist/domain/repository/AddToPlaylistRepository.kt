package com.pierre.tunescout.feature.addtoplaylist.domain.repository

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

interface AddToPlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>

    fun observeSong(songId: Long): Flow<Song?>

    suspend fun addSong(
        playlistId: Long,
        song: Song,
    )

    suspend fun createPlaylist(name: String): Long
}
