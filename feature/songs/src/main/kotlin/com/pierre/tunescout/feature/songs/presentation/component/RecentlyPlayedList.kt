package com.pierre.tunescout.feature.songs.presentation.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.SwipeToRemoveBox
import com.pierre.tunescout.ui.component.getNowPlayingState
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

@Composable
internal fun RecentlyPlayedList(
    songs: List<Song>,
    nowPlayingId: Long?,
    isPlaying: Boolean,
    songPendingRemoval: Song?,
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
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.large,
            end = TuneScoutSpacing.medium,
            top = TuneScoutSpacing.small,
            bottom = TuneScoutSpacing.extraLarge,
        ),
    ) {
        item(key = "header") {
            Text(
                text = stringResource(R.string.songs_recently_played),
                style = MaterialTheme.typography.bodySmall,
                color = TuneScoutColors.textSecondary,
                modifier = Modifier.padding(vertical = TuneScoutSpacing.small),
            )
        }
        items(items = songs, key = { song -> song.id }) { song ->
            SwipeToRemoveBox(
                onRemove = { onEvent(SongsUiEvent.OnRecentSongSwipedAway(song)) },
                modifier = Modifier.animateItem(),
                isRemovalPending = song == songPendingRemoval,
            ) {
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    nowPlaying = getNowPlayingState(
                        isCurrentSong = song.id == nowPlayingId,
                        isPlaying = isPlaying,
                    ),
                    sharedSongId = song.id,
                    onClick = { onEvent(SongsUiEvent.OnSongClicked(song)) },
                    trailing = { SongRowMoreAction { onEvent(SongsUiEvent.OnSongOptionsClicked(song)) } },
                )
            }
        }
    }
}
