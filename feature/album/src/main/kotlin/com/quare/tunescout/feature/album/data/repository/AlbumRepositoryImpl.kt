package com.quare.tunescout.feature.album.data.repository

import com.quare.tunescout.core.database.AlbumLocalDataSource
import com.quare.tunescout.core.model.Album
import com.quare.tunescout.core.network.ITunesRemoteDataSource
import com.quare.tunescout.core.utils.suspendRunCatching
import com.quare.tunescout.feature.album.domain.repository.AlbumRepository
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
