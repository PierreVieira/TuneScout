package com.pierre.tunescout.feature.songs.presentation.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.ui.component.DownloadIndicator
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowAction
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.SongSwipeActionsBox
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_SONG = "song"

@Composable
internal fun RecentlyPlayedList(
    songs: List<Song>,
    listState: LazyListState,
    nowPlaying: NowPlaying?,
    favoriteSongIds: Set<Long>,
    unplayableSongIds: Set<Long>,
    downloadStatuses: Map<Long, SongDownloadStatus>,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (songs.isEmpty()) {
        StateMessage(
            title = stringResource(R.string.songs_empty_title),
            description = stringResource(R.string.songs_empty_description),
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.large,
            end = TuneScoutSpacing.medium,
            top = TuneScoutSpacing.small,
            bottom = TuneScoutSpacing.extraLarge,
        ),
    ) {
        item(key = "header", contentType = CONTENT_TYPE_HEADER) {
            Text(
                text = stringResource(R.string.songs_recently_played),
                style = MaterialTheme.typography.bodySmall,
                color = TuneScoutColors.textSecondary,
                modifier = Modifier
                    .padding(vertical = TuneScoutSpacing.small)
                    .semantics { heading() },
            )
        }
        items(items = songs, key = { song -> song.id }, contentType = { CONTENT_TYPE_SONG }) { song ->
            SongSwipeActionsBox(
                isFavorite = song.id in favoriteSongIds,
                onAddToQueue = { onEvent(SongsUiEvent.OnSongSwipedToQueue(song)) },
                onToggleFavorite = { onEvent(SongsUiEvent.OnSongSwipedToFavorite(song)) },
                modifier = Modifier.animateItem(),
            ) {
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    nowPlaying = NowPlayingState.of(
                        isCurrentSong = nowPlaying.isOn(song.id),
                        isPlaying = nowPlaying?.isPlaying == true,
                    ),
                    isUnavailable = song.id in unplayableSongIds,
                    downloadIndicator = DownloadIndicator.of(
                        isRequested = song.id in downloadStatuses,
                        isComplete = downloadStatuses[song.id] == SongDownloadStatus.Downloaded,
                    ),
                    sharedSongId = song.id,
                    onClick = { onEvent(SongsUiEvent.OnSongClicked(song)) },
                    trailing = {
                        SongRowAction(
                            icon = TuneScoutIcons.removeFromQueue,
                            contentDescription = stringResource(R.string.songs_remove_recent),
                            onClick = { onEvent(SongsUiEvent.OnRemoveRecentClicked(song)) },
                        )
                        SongRowMoreAction { onEvent(SongsUiEvent.OnSongOptionsClicked(song)) }
                    },
                )
            }
        }
    }
}
