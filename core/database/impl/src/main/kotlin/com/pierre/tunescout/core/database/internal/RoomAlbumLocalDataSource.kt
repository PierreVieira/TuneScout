package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.mapper.toAlbum
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

internal class RoomAlbumLocalDataSource(
    private val albumDao: AlbumDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
) : AlbumLocalDataSource {
    override suspend fun save(album: Album) {
        val cachedAt = timestampProvider.provide()
        songDao.upsertAll(album.songs.map { song -> song.toEntity(cachedAt = cachedAt) })
        albumDao.upsert(album.toEntity(cachedAt = cachedAt))
    }

    override fun observe(albumId: Long): Flow<Album?> =
        albumDao.observeWithSongs(albumId).map { relation -> relation?.toAlbum() }

    override suspend fun isFresherThan(
        albumId: Long,
        maxAge: Duration,
    ): Boolean {
        val cachedAt = albumDao.findCachedAt(albumId) ?: return false
        return timestampProvider.provide() - cachedAt < maxAge.inWholeMilliseconds
    }
}
