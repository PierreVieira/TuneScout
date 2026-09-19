package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.database.dao.FavoriteAlbumDao
import com.pierre.tunescout.core.database.entity.FavoriteAlbumEntity
import com.pierre.tunescout.core.database.mapper.toSummary
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFavoriteAlbumLocalDataSource(
    private val favoriteAlbumDao: FavoriteAlbumDao,
    private val albumLocalDataSource: AlbumLocalDataSource,
    private val timestampProvider: TimestampProvider,
) : FavoriteAlbumLocalDataSource {
    override fun observeAll(): Flow<List<AlbumSummary>> =
        favoriteAlbumDao.observeAll().map { entities -> entities.map { entity -> entity.toSummary() } }

    override fun observeIsFavorite(albumId: Long): Flow<Boolean> = favoriteAlbumDao.observeContains(albumId)

    override suspend fun add(album: Album) {
        albumLocalDataSource.save(album)
        favoriteAlbumDao.upsert(
            FavoriteAlbumEntity(albumId = album.id, favoritedAt = timestampProvider.provide()),
        )
    }

    override suspend fun remove(albumId: Long) {
        favoriteAlbumDao.deleteByAlbumId(albumId)
    }
}
