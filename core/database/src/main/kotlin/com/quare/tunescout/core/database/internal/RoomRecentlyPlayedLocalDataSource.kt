package com.quare.tunescout.core.database.internal

import com.quare.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.quare.tunescout.core.database.dao.RecentlyPlayedDao
import com.quare.tunescout.core.database.dao.SongDao
import com.quare.tunescout.core.database.entity.RecentlyPlayedEntity
import com.quare.tunescout.core.database.mapper.toEntity
import com.quare.tunescout.core.database.mapper.toSong
import com.quare.tunescout.core.model.Song
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
}
