package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.database.dao.DownloadDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.DownloadedSongEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongExclusionEntity
import com.pierre.tunescout.core.database.mapper.toDownloadedCollectionEntityOrNull
import com.pierre.tunescout.core.database.mapper.toDownloadedCollectionId
import com.pierre.tunescout.core.database.mapper.toDownloadedCollectionKindOrNull
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toLibraryItemKeyOrNull
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class RoomDownloadLocalDataSource(
    private val downloadDao: DownloadDao,
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
) : DownloadLocalDataSource {
    override fun observeWantedSongs(): Flow<List<Song>> = downloadDao
        .observeWantedSongs()
        .map { entities -> entities.map { entity -> entity.toSong() } }
        .distinctUntilChanged()

    override fun observeIsWanted(songId: Long): Flow<Boolean> =
        downloadDao.observeIsWanted(songId).distinctUntilChanged()

    override fun observeOwnSongs(): Flow<List<Song>> = downloadDao
        .observeOwnSongs()
        .map { entities -> entities.map { entity -> entity.toSong() } }
        .distinctUntilChanged()

    override fun observeCollections(): Flow<Set<LibraryItemKey>> = downloadDao
        .observeCollections()
        .map { entities -> entities.mapNotNullTo(mutableSetOf()) { entity -> entity.toLibraryItemKeyOrNull() } }
        .distinctUntilChanged()

    override suspend fun addSong(song: Song) {
        songDao.upsertAll(listOf(song.toEntity(cachedAt = timestampProvider.provide())))
        downloadDao.upsertSong(DownloadedSongEntity(songId = song.id, requestedAt = timestampProvider.provide()))
        downloadDao.deleteExclusion(song.id)
    }

    /**
     * Excluding the song, not just dropping its own request, is what takes it off the device even
     * while an album, a playlist or the liked songs still want it — otherwise one of them would
     * still hold it.
     */
    override suspend fun removeSong(songId: Long) {
        downloadDao.deleteSong(songId)
        val exclusion = DownloadedSongExclusionEntity(songId = songId, excludedAt = timestampProvider.provide())
        downloadDao.upsertExclusion(exclusion)
    }

    override suspend fun addCollection(key: LibraryItemKey) {
        val entry = key.toDownloadedCollectionEntityOrNull(requestedAt = timestampProvider.provide()) ?: return
        downloadDao.upsertCollection(entry)
    }

    override suspend fun removeCollection(key: LibraryItemKey) {
        val kind = key.toDownloadedCollectionKindOrNull() ?: return
        downloadDao.deleteCollectionWithItsSongs(
            kind = kind.name,
            collectionId = key.toDownloadedCollectionId(),
        )
    }
}
