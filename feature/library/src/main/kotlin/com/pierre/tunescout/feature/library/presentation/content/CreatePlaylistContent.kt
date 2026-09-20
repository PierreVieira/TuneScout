package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.CreatePlaylistUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CreatePlaylistUiState
import com.pierre.tunescout.ui.component.NamePromptCard

@Composable
fun CreatePlaylistContent(
    uiState: CreatePlaylistUiState,
    onEvent: (CreatePlaylistUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NamePromptCard(
        title = stringResource(R.string.library_create_playlist_title),
        placeholder = stringResource(R.string.library_create_playlist_placeholder),
        confirmLabel = stringResource(R.string.library_create_playlist_confirm),
        cancelLabel = stringResource(R.string.library_create_playlist_cancel),
        name = uiState.name,
        canConfirm = uiState.canConfirm,
        onNameChange = { name -> onEvent(CreatePlaylistUiEvent.OnNameChanged(name)) },
        onConfirm = { onEvent(CreatePlaylistUiEvent.OnConfirmClicked) },
        onCancel = { onEvent(CreatePlaylistUiEvent.OnDismissed) },
        modifier = modifier,
    )
}
