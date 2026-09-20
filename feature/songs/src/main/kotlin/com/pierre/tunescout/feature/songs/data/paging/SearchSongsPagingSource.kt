package com.pierre.tunescout.feature.songs.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.network.RemoteException

internal const val MAX_SEARCH_RESULTS = 200

internal class SearchSongsPagingSource(
    private val remoteDataSource: ITunesRemoteDataSource,
    private val songLocalDataSource: SongLocalDataSource,
    private val term: String,
) : PagingSource<Int, Song>() {
    private val deliveredIds = mutableSetOf<Long>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        val alreadyDelivered = params.key ?: 0
        val limit = minOf(alreadyDelivered + params.loadSize, MAX_SEARCH_RESULTS)
        return try {
            val results = remoteDataSource.searchSongs(
                term = term,
                limit = limit,
                forceRefresh = params is LoadParams.Refresh,
            )
            val newSongs = results.drop(alreadyDelivered).filter { song -> deliveredIds.add(song.id) }
            songLocalDataSource.save(newSongs)
            val reachedEnd = results.size < limit || limit >= MAX_SEARCH_RESULTS
            LoadResult.Page(
                data = newSongs,
                prevKey = null,
                nextKey = if (reachedEnd) null else results.size,
            )
        } catch (exception: RemoteException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? = null
}
