package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room's side of the song cache: every song that ever appeared on screen, so the player and the
 * album screen open with no connection.
 *
 * @property songDao the table itself.
 * @property timestampProvider stamps each saved song, which is what orders the cache and decides
 * what is dropped first.
 * @property maxCachedSongs how many songs nothing else points at are kept. Every search result is
 * written here, so the table would otherwise grow with every query the user ever typed.
 */
internal class RoomSongLocalDataSource(
    private val songDao: SongDao,
    private val timestampProvider: TimestampProvider,
    private val maxCachedSongs: Int,
) : SongLocalDataSource {
    override suspend fun save(songs: List<Song>) {
        val cachedAt = timestampProvider.provide()
        songDao.upsertAllAndTrim(
            songs = songs.map { song -> song.toEntity(cachedAt = cachedAt) },
            keep = maxCachedSongs,
        )
    }

    override fun observe(songId: Long): Flow<Song?> = songDao.observeById(songId).map { entity -> entity?.toSong() }

    override suspend fun find(songId: Long): Song? = songDao.getById(songId)?.toSong()

    override suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<Song> = songDao.findByTerm(term = term, limit = limit).map { entity -> entity.toSong() }
}
