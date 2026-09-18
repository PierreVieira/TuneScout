package com.quare.tunescout.feature.songs.presentation.component

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
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.feature.songs.R
import com.quare.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.quare.tunescout.ui.component.SongRow
import com.quare.tunescout.ui.component.StateMessage
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing

@Composable
internal fun RecentlyPlayedList(
    songs: List<Song>,
    nowPlayingId: Long?,
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
            SongRow(
                title = song.title,
                subtitle = song.artistName,
                artworkUrl = song.artworkUrl,
                isHighlighted = song.id == nowPlayingId,
                onClick = { onEvent(SongsUiEvent.OnSongClicked(song = song, queue = songs)) },
                onMoreClick = { onEvent(SongsUiEvent.OnSongOptionsClicked(song)) },
            )
        }
    }
}
