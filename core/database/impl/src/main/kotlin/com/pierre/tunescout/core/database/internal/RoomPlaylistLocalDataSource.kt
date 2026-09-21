package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.database.dao.PlaylistDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.PlaylistEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toPlaylists
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomPlaylistLocalDataSource(
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
) : PlaylistLocalDataSource {
    override fun observeAll(): Flow<List<Playlist>> = playlistDao.observeAllRows().map { rows -> rows.toPlaylists() }

    override fun observe(playlistId: Long): Flow<Playlist?> =
        playlistDao.observeRows(playlistId).map { rows -> rows.toPlaylists().firstOrNull() }

    override fun observeSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observeSongs(playlistId).map { entities -> entities.map { entity -> entity.toSong() } }

    override fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean> = playlistDao.observeContains(playlistId = playlistId, songId = songId)

    override suspend fun create(name: String): Long = playlistDao.insert(
        PlaylistEntity(id = 0, name = name, createdAt = timestampProvider.provide()),
    )

    override suspend fun rename(
        playlistId: Long,
        name: String,
    ) {
        playlistDao.updateName(playlistId = playlistId, name = name)
    }

    override suspend fun delete(playlistId: Long) {
        playlistDao.deleteById(playlistId)
    }

    override suspend fun addSong(
        playlistId: Long,
        song: Song,
    ) {
        songDao.upsertAll(listOf(song.toEntity(cachedAt = timestampProvider.provide())))
        playlistDao.appendSong(playlistId = playlistId, songId = song.id)
    }

    override suspend fun removeSong(
        playlistId: Long,
        songId: Long,
    ) {
        playlistDao.deleteSong(playlistId = playlistId, songId = songId)
    }

    override suspend fun reorderSongs(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        playlistDao.reorderSongs(playlistId = playlistId, songIds = songIds)
    }
}
