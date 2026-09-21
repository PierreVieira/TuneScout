package com.pierre.tunescout.feature.songoptions.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.songoptions.R
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import com.pierre.tunescout.ui.component.OptionRow
import com.pierre.tunescout.ui.component.OptionsSheet
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.component.R as ComponentR

@Composable
fun SongOptionsContent(
    uiState: SongOptionsUiState,
    onEvent: (SongOptionsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionsSheet(
        title = uiState.song?.title.orEmpty(),
        subtitle = uiState.song?.artistName.orEmpty(),
        modifier = modifier,
    ) {
        val isEnabled = uiState.song != null
        OptionRow(
            icon = if (uiState.isFavorite) TuneScoutIcons.favoriteFilled else TuneScoutIcons.favorite,
            label = stringResource(
                if (uiState.isFavorite) R.string.song_options_unfavorite else R.string.song_options_favorite,
            ),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnFavoriteClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.addToPlaylist,
            label = stringResource(R.string.song_options_add_to_playlist),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnAddToPlaylistClicked) },
        )
        OptionRow(
            icon = if (uiState.isDownloaded) TuneScoutIcons.downloaded else TuneScoutIcons.download,
            label = stringResource(
                if (uiState.isDownloaded) ComponentR.string.ui_remove_download else ComponentR.string.ui_download,
            ),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnDownloadClicked) },
        )
        if (uiState.isRemovableFromPlaylist) {
            OptionRow(
                icon = TuneScoutIcons.removeFromPlaylist,
                label = stringResource(R.string.song_options_remove_from_playlist),
                isEnabled = isEnabled,
                onClick = { onEvent(SongOptionsUiEvent.OnRemoveFromPlaylistClicked) },
            )
        }
        if (uiState.isReorderable) {
            OptionRow(
                icon = TuneScoutIcons.reorder,
                label = stringResource(ComponentR.string.ui_reorder_songs),
                isEnabled = isEnabled,
                onClick = { onEvent(SongOptionsUiEvent.OnReorderClicked) },
            )
        }
        OptionRow(
            icon = TuneScoutIcons.play,
            label = stringResource(R.string.song_options_play_now),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnPlayNowClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.queueNext,
            label = stringResource(R.string.song_options_play_next),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnPlayNextClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.addToQueue,
            label = stringResource(R.string.song_options_add_to_queue),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnAddToQueueClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.viewAlbum,
            label = stringResource(R.string.song_options_view_album),
            isEnabled = isEnabled,
            onClick = { onEvent(SongOptionsUiEvent.OnViewAlbumClicked) },
        )
    }
}
