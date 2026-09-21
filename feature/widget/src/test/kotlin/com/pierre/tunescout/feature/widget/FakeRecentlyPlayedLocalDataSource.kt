package com.pierre.tunescout.feature.widget

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A source of recent songs that also records every limit [observe] was asked for.
 *
 * @property songs the recent songs, newest first.
 */
class FakeRecentlyPlayedLocalDataSource(
    private val songs: List<Song>,
) : RecentlyPlayedLocalDataSource {
    val requestedLimits: MutableList<Int> = mutableListOf()

    override fun observe(limit: Int): Flow<List<Song>> {
        requestedLimits += limit
        return flowOf(songs.take(limit))
    }

    override fun observeIsRecentlyPlayed(songId: Long): Flow<Boolean> = flowOf(songs.any { song -> song.id == songId })

    override suspend fun record(song: Song) {
        error("unused")
    }

    override suspend fun remove(songId: Long) {
        error("unused")
    }
}
