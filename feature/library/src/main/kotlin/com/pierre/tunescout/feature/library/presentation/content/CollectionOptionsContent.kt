package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiState
import com.pierre.tunescout.ui.component.ConfirmationDialog
import com.pierre.tunescout.ui.component.OptionRow
import com.pierre.tunescout.ui.component.OptionsSheet
import com.pierre.tunescout.ui.component.TuneScoutIcons

@Composable
fun CollectionOptionsContent(
    uiState: CollectionOptionsUiState,
    onEvent: (CollectionOptionsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = uiState.title?.let { title -> collectionTitleText(title) }.orEmpty()
    OptionsSheet(
        title = title,
        subtitle = pluralStringResource(R.plurals.library_song_count, uiState.songs.size, uiState.songs.size),
        modifier = modifier,
    ) {
        val isEnabled = uiState.songs.isNotEmpty()
        OptionRow(
            icon = TuneScoutIcons.queueNext,
            label = stringResource(R.string.library_collection_play_next),
            isEnabled = isEnabled,
            onClick = { onEvent(CollectionOptionsUiEvent.OnPlayNextClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.addToQueue,
            label = stringResource(R.string.library_collection_add_to_queue),
            isEnabled = isEnabled,
            onClick = { onEvent(CollectionOptionsUiEvent.OnAddToQueueClicked) },
        )
        if (uiState.isDeletable) {
            OptionRow(
                icon = TuneScoutIcons.delete,
                label = stringResource(R.string.library_delete_playlist),
                isEnabled = true,
                onClick = { onEvent(CollectionOptionsUiEvent.OnDeleteClicked) },
            )
        }
    }
    if (uiState.isConfirmingDelete) {
        ConfirmationDialog(
            title = stringResource(R.string.library_delete_playlist_confirm_title),
            message = stringResource(R.string.library_delete_playlist_confirm_message, title),
            confirmLabel = stringResource(R.string.library_delete_playlist_confirm_action),
            cancelLabel = stringResource(R.string.library_confirm_cancel),
            onConfirm = { onEvent(CollectionOptionsUiEvent.OnDeleteConfirmed) },
            onCancel = { onEvent(CollectionOptionsUiEvent.OnDeleteDismissed) },
        )
    }
}
