package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.album.R
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiState
import com.pierre.tunescout.ui.component.OptionRow
import com.pierre.tunescout.ui.component.OptionsSheet
import com.pierre.tunescout.ui.component.TuneScoutIcons

@Composable
fun AlbumOptionsContent(
    uiState: AlbumOptionsUiState,
    onEvent: (AlbumOptionsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionsSheet(
        title = uiState.album?.title.orEmpty(),
        subtitle = uiState.album?.artistName.orEmpty(),
        modifier = modifier,
    ) {
        val isEnabled = uiState.album != null
        OptionRow(
            icon = TuneScoutIcons.queueNext,
            label = stringResource(R.string.album_play_next),
            isEnabled = isEnabled,
            onClick = { onEvent(AlbumOptionsUiEvent.OnPlayNextClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.addToQueue,
            label = stringResource(R.string.album_add_to_queue),
            isEnabled = isEnabled,
            onClick = { onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked) },
        )
    }
}
