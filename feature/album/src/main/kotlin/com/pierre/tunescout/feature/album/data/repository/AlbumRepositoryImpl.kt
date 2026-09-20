package com.pierre.tunescout.feature.album.data.repository

import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.utils.suspendRunCatching
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Reads the album from the device and refreshes it from the API behind it.
 *
 * @property remoteDataSource the iTunes lookup endpoint.
 * @property albumLocalDataSource the cached album and its tracks.
 * @property cacheMaxAge how long a cached album is served without asking the API again. An album's
 * track list does not change, so re-opening one minutes later is a call that would return exactly
 * the rows already on screen — and one the throttling limit could refuse.
 */
internal class AlbumRepositoryImpl(
    private val remoteDataSource: ITunesRemoteDataSource,
    private val albumLocalDataSource: AlbumLocalDataSource,
    private val cacheMaxAge: Duration,
) : AlbumRepository {
    override fun observeAlbum(albumId: Long): Flow<Album?> = albumLocalDataSource.observe(albumId)

    override suspend fun refreshAlbum(albumId: Long): Result<Unit> = suspendRunCatching {
        if (albumLocalDataSource.isFresherThan(albumId = albumId, maxAge = cacheMaxAge)) {
            return@suspendRunCatching
        }
        val album = remoteDataSource.fetchAlbum(albumId) ?: throw AlbumNotFoundException(albumId)
        albumLocalDataSource.save(album)
    }
}
