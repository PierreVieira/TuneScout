package com.pierre.tunescout.feature.songoptions.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.songoptions.R
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val menuItemHeight = 56.dp
private val menuIconSize = 24.dp
private val bottomPadding = 32.dp

@Composable
fun SongOptionsContent(
    uiState: SongOptionsUiState,
    onEvent: (SongOptionsUiEvent) -> Unit,
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
            text = uiState.song?.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = TuneScoutSpacing.large),
        )
        Text(
            text = uiState.song?.artistName.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                top = TuneScoutSpacing.small,
                start = TuneScoutSpacing.large,
                end = TuneScoutSpacing.large,
            ),
        )
        OptionRow(
            icon = if (uiState.isFavorite) TuneScoutIcons.favoriteFilled else TuneScoutIcons.favorite,
            label = stringResource(
                if (uiState.isFavorite) R.string.song_options_unfavorite else R.string.song_options_favorite,
            ),
            isEnabled = uiState.song != null,
            onClick = { onEvent(SongOptionsUiEvent.OnFavoriteClicked) },
            modifier = Modifier.padding(top = TuneScoutSpacing.large),
        )
        OptionRow(
            icon = TuneScoutIcons.addToPlaylist,
            label = stringResource(R.string.song_options_add_to_playlist),
            isEnabled = uiState.song != null,
            onClick = { onEvent(SongOptionsUiEvent.OnAddToPlaylistClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.queueNext,
            label = stringResource(R.string.song_options_play_next),
            isEnabled = uiState.song != null,
            onClick = { onEvent(SongOptionsUiEvent.OnPlayNextClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.addToQueue,
            label = stringResource(R.string.song_options_add_to_queue),
            isEnabled = uiState.song != null,
            onClick = { onEvent(SongOptionsUiEvent.OnAddToQueueClicked) },
        )
        OptionRow(
            icon = TuneScoutIcons.viewAlbum,
            label = stringResource(R.string.song_options_view_album),
            isEnabled = uiState.song != null,
            onClick = { onEvent(SongOptionsUiEvent.OnViewAlbumClicked) },
        )
        if (uiState.isRecentlyPlayed) {
            OptionRow(
                icon = TuneScoutIcons.delete,
                label = stringResource(R.string.song_options_remove_recent),
                isEnabled = true,
                onClick = { onEvent(SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked) },
            )
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(menuItemHeight)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = TuneScoutSpacing.large + TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(menuIconSize),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.textPrimary,
        )
    }
}
