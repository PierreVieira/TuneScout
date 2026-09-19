package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomRecentlyPlayedLocalDataSource(
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
    private val maxEntries: Int,
) : RecentlyPlayedLocalDataSource {
    override fun observe(limit: Int): Flow<List<Song>> =
        recentlyPlayedDao.observeMostRecent(limit).map { entities -> entities.map { entity -> entity.toSong() } }

    override suspend fun record(song: Song) {
        songDao.upsertAll(listOf(song.toEntity()))
        recentlyPlayedDao.record(
            entry = RecentlyPlayedEntity(songId = song.id, playedAt = timestampProvider.provide()),
            keep = maxEntries,
        )
    }

    override suspend fun remove(songId: Long) {
        recentlyPlayedDao.deleteBySongId(songId)
    }
}
