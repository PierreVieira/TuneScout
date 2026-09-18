package com.pierre.tunescout.feature.album.data.repository

import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.utils.suspendRunCatching
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow

internal class AlbumRepositoryImpl(
    private val remoteDataSource: ITunesRemoteDataSource,
    private val albumLocalDataSource: AlbumLocalDataSource,
) : AlbumRepository {
    override fun observeAlbum(albumId: Long): Flow<Album?> = albumLocalDataSource.observe(albumId)

    override suspend fun refreshAlbum(albumId: Long): Result<Unit> = suspendRunCatching {
        val album = remoteDataSource.fetchAlbum(albumId) ?: throw AlbumNotFoundException(albumId)
        albumLocalDataSource.save(album)
    }
}
