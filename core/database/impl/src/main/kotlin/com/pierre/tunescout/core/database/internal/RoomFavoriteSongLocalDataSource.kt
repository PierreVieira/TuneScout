package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.dao.FavoriteSongDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.FavoriteSongEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFavoriteSongLocalDataSource(
    private val favoriteSongDao: FavoriteSongDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
) : FavoriteSongLocalDataSource {
    override fun observeAll(): Flow<List<Song>> =
        favoriteSongDao.observeAll().map { entities -> entities.map { entity -> entity.toSong() } }

    override fun observeIsFavorite(songId: Long): Flow<Boolean> = favoriteSongDao.observeContains(songId)

    override suspend fun add(song: Song) {
        songDao.upsertAll(listOf(song.toEntity(cachedAt = timestampProvider.provide())))
        favoriteSongDao.upsert(
            FavoriteSongEntity(songId = song.id, favoritedAt = timestampProvider.provide()),
        )
    }

    override suspend fun remove(songId: Long) {
        favoriteSongDao.deleteBySongId(songId)
    }
}
