package com.quare.tunescout.core.database.internal

import com.quare.tunescout.core.database.AlbumLocalDataSource
import com.quare.tunescout.core.database.dao.AlbumDao
import com.quare.tunescout.core.database.dao.SongDao
import com.quare.tunescout.core.database.mapper.toAlbum
import com.quare.tunescout.core.database.mapper.toEntity
import com.quare.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomAlbumLocalDataSource(
    private val albumDao: AlbumDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
) : AlbumLocalDataSource {
    override suspend fun save(album: Album) {
        songDao.upsertAll(album.songs.map { song -> song.toEntity() })
        albumDao.upsert(album.toEntity(cachedAt = timestampProvider.provide()))
    }

    override fun observe(albumId: Long): Flow<Album?> =
        albumDao.observeWithSongs(albumId).map { relation -> relation?.toAlbum() }
}
