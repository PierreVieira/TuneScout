package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomSongLocalDataSource(
    private val songDao: SongDao,
) : SongLocalDataSource {
    override suspend fun save(songs: List<Song>) {
        songDao.upsertAll(songs.map { song -> song.toEntity() })
    }

    override fun observe(songId: Long): Flow<Song?> = songDao.observeById(songId).map { entity -> entity?.toSong() }

    override suspend fun find(songId: Long): Song? = songDao.getById(songId)?.toSong()
}
