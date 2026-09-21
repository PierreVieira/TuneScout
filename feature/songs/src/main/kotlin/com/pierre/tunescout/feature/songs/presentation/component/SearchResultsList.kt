package com.pierre.tunescout.feature.songs.presentation.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.network.RemoteException
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.SongListSkeleton
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private const val APPEND_SKELETON_ROWS = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchResultsList(
    searchResults: LazyPagingItems<SearchResultUiModel>,
    nowPlaying: NowPlaying?,
    isOffline: Boolean,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = searchResults.loadState.refresh
    val appendState = searchResults.loadState.append
    PullToRefreshBox(
        isRefreshing = refreshState is LoadState.Loading && searchResults.itemCount > 0,
        onRefresh = searchResults::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = TuneScoutSpacing.large,
                end = TuneScoutSpacing.medium,
                top = TuneScoutSpacing.small,
                bottom = TuneScoutSpacing.extraLarge,
            ),
        ) {
            when {
                refreshState is LoadState.Loading && searchResults.itemCount == 0 -> item(key = "loading") {
                    SongListSkeleton(hasMoreAction = true)
                }

                refreshState is LoadState.Error -> item(key = "error") {
                    ErrorMessage(
                        error = refreshState.error,
                        isOffline = isOffline,
                        onRetry = searchResults::retry,
                    )
                }

                refreshState is LoadState.NotLoading && searchResults.itemCount == 0 -> item(key = "empty") {
                    StateMessage(
                        title = stringResource(R.string.songs_no_results_title),
                        description = stringResource(R.string.songs_no_results_description),
                    )
                }
            }
            items(
                count = searchResults.itemCount,
                key = searchResults.itemKey { result -> result.song.id },
            ) { index ->
                val result = searchResults[index] ?: return@items
                val song = result.song
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    nowPlaying = NowPlayingState.of(
                        isCurrentSong = song.id == nowPlaying?.songId,
                        isPlaying = nowPlaying?.isPlaying == true,
                    ),
                    isUnavailable = result.isUnavailable,
                    sharedSongId = song.id,
                    onClick = {
                        onEvent(SongsUiEvent.OnSongClicked(song))
                    },
                    trailing = { SongRowMoreAction { onEvent(SongsUiEvent.OnSongOptionsClicked(song)) } },
                )
            }
            when (appendState) {
                is LoadState.Loading -> item(key = "appending") {
                    SongListSkeleton(rows = APPEND_SKELETON_ROWS, hasMoreAction = true)
                }

                is LoadState.Error -> item(key = "append-error") {
                    ErrorMessage(
                        error = appendState.error,
                        isOffline = isOffline,
                        onRetry = searchResults::retry,
                    )
                }

                is LoadState.NotLoading -> Unit
            }
        }
    }
}

/**
 * The device's own answer on whether it is [isOffline] decides the wording, instead of reading the
 * failure as one: a timeout, a bad response and no connection at all used to say the same thing.
 */
@Composable
private fun ErrorMessage(
    error: Throwable,
    isOffline: Boolean,
    onRetry: () -> Unit,
) {
    val description = when {
        error is RemoteException.RateLimited -> stringResource(R.string.songs_error_rate_limited)
        isOffline -> stringResource(R.string.songs_error_offline)
        else -> stringResource(R.string.songs_error_generic)
    }
    StateMessage(
        title = stringResource(R.string.songs_error_title),
        description = description,
        onRetry = onRetry,
    )
}
