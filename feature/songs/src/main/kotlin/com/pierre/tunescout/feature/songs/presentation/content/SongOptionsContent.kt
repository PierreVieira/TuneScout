package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiState
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
        Row(
            modifier = Modifier
                .padding(top = TuneScoutSpacing.large)
                .fillMaxWidth()
                .height(menuItemHeight)
                .clickable(enabled = uiState.song != null) { onEvent(SongOptionsUiEvent.OnViewAlbumClicked) }
                .padding(horizontal = TuneScoutSpacing.large + TuneScoutSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(TuneScoutIcons.viewAlbum),
                contentDescription = null,
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier.size(menuIconSize),
            )
            Text(
                text = stringResource(R.string.songs_options_view_album),
                style = MaterialTheme.typography.bodyLarge,
                color = TuneScoutColors.textPrimary,
            )
        }
    }
}
