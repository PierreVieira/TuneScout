package com.pierre.tunescout.feature.addtoplaylist.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.feature.addtoplaylist.R
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiEvent
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiState
import com.pierre.tunescout.ui.component.NamePromptCard
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val rowHeight = 56.dp
private val rowIconSize = 24.dp
private val bottomPadding = 32.dp

@Composable
fun AddToPlaylistContent(
    uiState: AddToPlaylistUiState,
    onEvent: (AddToPlaylistUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.add_to_playlist_title),
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = TuneScoutSpacing.large)
                .semantics { heading() },
        )
        OptionRow(
            label = stringResource(R.string.add_to_playlist_new),
            supporting = null,
            isEnabled = uiState.song != null,
            onClick = { onEvent(AddToPlaylistUiEvent.OnNewPlaylistClicked) },
            modifier = Modifier.padding(top = TuneScoutSpacing.large),
        )
        if (uiState.playlists.isEmpty()) {
            Text(
                text = stringResource(R.string.add_to_playlist_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = TuneScoutSpacing.large,
                    vertical = TuneScoutSpacing.medium,
                ),
            )
        }
        uiState.playlists.forEach { playlist ->
            OptionRow(
                label = playlist.name,
                supporting = songCountText(playlist),
                isEnabled = uiState.song != null,
                onClick = { onEvent(AddToPlaylistUiEvent.OnPlaylistClicked(playlistId = playlist.id)) },
            )
        }
    }
    if (uiState.isPromptOpen) {
        NewPlaylistDialog(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun NewPlaylistDialog(
    uiState: AddToPlaylistUiState,
    onEvent: (AddToPlaylistUiEvent) -> Unit,
) {
    Dialog(onDismissRequest = { onEvent(AddToPlaylistUiEvent.OnNewPlaylistDismissed) }) {
        NamePromptCard(
            title = stringResource(R.string.add_to_playlist_prompt_title),
            placeholder = stringResource(R.string.add_to_playlist_prompt_placeholder),
            confirmLabel = stringResource(R.string.add_to_playlist_prompt_confirm),
            cancelLabel = stringResource(R.string.add_to_playlist_prompt_cancel),
            name = uiState.newPlaylistName.orEmpty(),
            canConfirm = uiState.canConfirmNewPlaylist,
            onNameChange = { name -> onEvent(AddToPlaylistUiEvent.OnNewPlaylistNameChanged(name)) },
            onConfirm = { onEvent(AddToPlaylistUiEvent.OnNewPlaylistConfirmed) },
            onCancel = { onEvent(AddToPlaylistUiEvent.OnNewPlaylistDismissed) },
        )
    }
}

@Composable
private fun OptionRow(
    label: String,
    supporting: String?,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = rowHeight)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = TuneScoutSpacing.large + TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (supporting == null) TuneScoutIcons.add else TuneScoutIcons.musicList,
            contentDescription = null,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(rowIconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TuneScoutColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = TuneScoutColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun songCountText(playlist: Playlist): String =
    pluralStringResource(R.plurals.add_to_playlist_song_count, playlist.songCount, playlist.songCount)
