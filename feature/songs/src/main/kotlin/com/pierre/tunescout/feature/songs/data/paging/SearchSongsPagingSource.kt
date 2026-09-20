package com.pierre.tunescout.feature.songs.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.network.RemoteException

internal const val MAX_SEARCH_RESULTS = 200
internal const val MAX_CACHED_RESULTS = 50

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
            loadFromCacheOrFail(exception = exception, alreadyDelivered = alreadyDelivered)
        }
    }

    /**
     * Only [RemoteException.Unavailable] falls back: it is the unreachable-API case, where the
     * songs already on the device are the best answer there is. A throttled or unexpected response
     * means the catalog is there and something else is wrong, and saying so is more useful than
     * quietly showing a subset of it.
     *
     * The cache answers the first page only. It is a single page by nature, so the list ends there
     * instead of asking the API again for a page it just failed to load.
     *
     * @return a last page of cached songs, or the failure itself when there is nothing to show.
     */
    private suspend fun loadFromCacheOrFail(
        exception: RemoteException,
        alreadyDelivered: Int,
    ): LoadResult<Int, Song> {
        if (exception !is RemoteException.Unavailable || alreadyDelivered > 0) {
            return LoadResult.Error(exception)
        }
        val cached = songLocalDataSource
            .findByTerm(term = term, limit = MAX_CACHED_RESULTS)
            .filter { song -> deliveredIds.add(song.id) }
        return if (cached.isEmpty()) {
            LoadResult.Error(exception)
        } else {
            LoadResult.Page(data = cached, prevKey = null, nextKey = null)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? = null
}
