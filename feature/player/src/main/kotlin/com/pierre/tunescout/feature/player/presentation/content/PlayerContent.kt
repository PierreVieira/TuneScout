package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.player.R
import com.pierre.tunescout.feature.player.presentation.component.PlaybackControls
import com.pierre.tunescout.feature.player.presentation.component.PlaybackTimeline
import com.pierre.tunescout.feature.player.presentation.component.PlayerSkeleton
import com.pierre.tunescout.feature.player.presentation.component.SongHeading
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.component.R as ComponentR

private val artworkTopSpacing = 100.dp
private val artworkSize = 264.dp
private val artworkCornerRadius = 32.dp

@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TopBar(
            title = (uiState as? PlayerUiState.Loaded)?.song?.albumTitle.orEmpty(),
            onBackClick = { onEvent(PlayerUiEvent.OnBackClicked) },
            actions = {
                if (uiState is PlayerUiState.Loaded) {
                    TopBarAction(
                        iconRes = TuneScoutIcons.moreMenu,
                        contentDescription = stringResource(ComponentR.string.ui_more_options),
                        onClick = { onEvent(PlayerUiEvent.OnMoreClicked) },
                    )
                }
            },
        )
        when (uiState) {
            PlayerUiState.Loading -> PlayerSkeleton(
                artworkTopSpacing = artworkTopSpacing,
                artworkSize = artworkSize,
                artworkCornerRadius = artworkCornerRadius,
            )

            PlayerUiState.NotFound -> StateMessage(
                title = stringResource(R.string.player_not_found_title),
                description = stringResource(R.string.player_not_found_description),
            )

            is PlayerUiState.Loaded -> LoadedContent(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun ColumnScope.LoadedContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
) {
    Spacer(modifier = Modifier.height(artworkTopSpacing))
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Artwork(
            url = uiState.song.artworkUrl,
            contentDescription = stringResource(ComponentR.string.ui_artwork_of, uiState.song.albumTitle),
            cornerRadius = artworkCornerRadius,
            modifier = Modifier.size(artworkSize),
        )
    }
    Spacer(modifier = Modifier.weight(1f))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
    ) {
        SongHeading(
            title = uiState.song.title,
            artistName = uiState.song.artistName,
            isRepeatEnabled = uiState.isRepeatEnabled,
            onRepeatClick = { onEvent(PlayerUiEvent.OnRepeatClicked) },
        )
        PlaybackTimeline(
            progress = uiState.progress,
            position = uiState.position,
            duration = uiState.duration,
            onSeekFinished = { fraction ->
                onEvent(PlayerUiEvent.OnSeekFinished(uiState.duration * fraction.toDouble()))
            },
        )
        PlaybackControls(
            isPlaying = uiState.isPlaying,
            hasPrevious = uiState.hasPrevious,
            hasNext = uiState.hasNext,
            onPlayPauseClick = { onEvent(PlayerUiEvent.OnPlayPauseClicked) },
            onPreviousClick = { onEvent(PlayerUiEvent.OnSkipPreviousClicked) },
            onNextClick = { onEvent(PlayerUiEvent.OnSkipNextClicked) },
        )
    }
}
