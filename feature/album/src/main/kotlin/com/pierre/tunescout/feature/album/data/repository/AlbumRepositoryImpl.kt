package com.pierre.tunescout.feature.album.data.repository

import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.utils.suspendRunCatching
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Reads the album from the device and refreshes it from the API behind it.
 *
 * @property remoteDataSource the iTunes lookup endpoint.
 * @property albumLocalDataSource the cached album and its tracks.
 * @property networkMonitor whether the API can be reached, so a screen showing what the device saved
 * can fetch the rest once the connection is back.
 * @property cacheMaxAge how long a cached album is served without asking the API again. An album's
 * track list does not change, so re-opening one minutes later is a call that would return exactly
 * the rows already on screen — and one the throttling limit could refuse.
 */
internal class AlbumRepositoryImpl(
    private val remoteDataSource: AlbumRemoteDataSource,
    private val albumLocalDataSource: AlbumLocalDataSource,
    private val networkMonitor: NetworkMonitor,
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

    override fun observeIsOnline(): Flow<Boolean> = networkMonitor.observeIsOnline()

    override suspend fun saveTrackOrder(
        albumId: Long,
        songIds: List<Long>,
    ) {
        albumLocalDataSource.saveTrackOrder(albumId = albumId, songIds = songIds)
    }
}
