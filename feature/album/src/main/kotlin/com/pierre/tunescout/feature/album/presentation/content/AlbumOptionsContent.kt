package com.pierre.tunescout.feature.album.presentation.content

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
import com.pierre.tunescout.feature.album.R
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiState
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val menuItemHeight = 56.dp
private val menuIconSize = 24.dp
private val bottomPadding = 32.dp

@Composable
fun AlbumOptionsContent(
    uiState: AlbumOptionsUiState,
    onEvent: (AlbumOptionsUiEvent) -> Unit,
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
            text = uiState.album?.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = TuneScoutSpacing.large),
        )
        Text(
            text = uiState.album?.artistName.orEmpty(),
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
            icon = TuneScoutIcons.queueNext,
            label = stringResource(R.string.album_play_next),
            isEnabled = uiState.album != null,
            onClick = { onEvent(AlbumOptionsUiEvent.OnPlayNextClicked) },
            modifier = Modifier.padding(top = TuneScoutSpacing.large),
        )
        OptionRow(
            icon = TuneScoutIcons.addToQueue,
            label = stringResource(R.string.album_add_to_queue),
            isEnabled = uiState.album != null,
            onClick = { onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked) },
        )
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
