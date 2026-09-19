package com.pierre.tunescout.feature.addtoplaylist.data.repository

import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import kotlinx.coroutines.flow.Flow

internal class AddToPlaylistRepositoryImpl(
    private val playlistLocalDataSource: PlaylistLocalDataSource,
    private val songLocalDataSource: SongLocalDataSource,
) : AddToPlaylistRepository {
    override fun observePlaylists(): Flow<List<Playlist>> = playlistLocalDataSource.observeAll()

    override fun observeSong(songId: Long): Flow<Song?> = songLocalDataSource.observe(songId)

    override suspend fun addSong(
        playlistId: Long,
        song: Song,
    ) {
        playlistLocalDataSource.addSong(playlistId = playlistId, song = song)
    }

    override suspend fun createPlaylist(name: String): Long = playlistLocalDataSource.create(name)
}
