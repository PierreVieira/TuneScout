package com.pierre.tunescout.feature.songs.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.feature.songs.data.paging.SearchSongsPagingSource
import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import kotlinx.coroutines.flow.Flow

internal class SongsRepositoryImpl(
    private val remoteDataSource: SongSearchRemoteDataSource,
    private val songLocalDataSource: SongLocalDataSource,
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
    private val networkMonitor: NetworkMonitor,
) : SongsRepository {
    override fun searchSongs(term: String): Flow<PagingData<Song>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            SearchSongsPagingSource(
                remoteDataSource = remoteDataSource,
                songLocalDataSource = songLocalDataSource,
                term = term,
            )
        },
    ).flow

    override fun observeRecentlyPlayed(): Flow<List<Song>> =
        recentlyPlayedLocalDataSource.observe(limit = RECENTLY_PLAYED_LIMIT)

    override fun observeIsOnline(): Flow<Boolean> = networkMonitor.observeIsOnline()

    override suspend fun removeFromRecentlyPlayed(songId: Long) {
        recentlyPlayedLocalDataSource.remove(songId)
    }

    private companion object {
        const val PAGE_SIZE = 25
        const val PREFETCH_DISTANCE = 5
        const val RECENTLY_PLAYED_LIMIT = 20
    }
}
