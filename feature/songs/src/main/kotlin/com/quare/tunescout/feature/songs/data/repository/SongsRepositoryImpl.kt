package com.quare.tunescout.feature.songs.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quare.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.quare.tunescout.core.database.SongLocalDataSource
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.core.network.ITunesRemoteDataSource
import com.quare.tunescout.feature.songs.data.paging.SearchSongsPagingSource
import com.quare.tunescout.feature.songs.domain.repository.SongsRepository
import kotlinx.coroutines.flow.Flow

private const val PAGE_SIZE = 25
private const val PREFETCH_DISTANCE = 5
private const val RECENTLY_PLAYED_LIMIT = 20

internal class SongsRepositoryImpl(
    private val remoteDataSource: ITunesRemoteDataSource,
    private val songLocalDataSource: SongLocalDataSource,
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
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
}
