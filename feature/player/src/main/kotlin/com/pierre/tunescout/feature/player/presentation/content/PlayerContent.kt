package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.player.R
import com.pierre.tunescout.feature.player.presentation.component.PlaybackControls
import com.pierre.tunescout.feature.player.presentation.component.PlaybackTimeline
import com.pierre.tunescout.feature.player.presentation.component.PlayerSkeleton
import com.pierre.tunescout.feature.player.presentation.component.SongHeading
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.SongSharedElement
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.component.getPlayButtonState
import com.pierre.tunescout.ui.component.getSongSharedKey
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.component.R as ComponentR

private val maxArtworkSize = 264.dp
private val minArtworkSize = 120.dp
private val maxArtworkTopSpacing = 100.dp
private const val ARTWORK_CORNER_PERCENT = 12
private val detailsHeight = 260.dp

@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    isSideBySide: Boolean,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        TopBar(
            title = stringResource(R.string.player_now_playing),
            onBackClick = { onEvent(PlayerUiEvent.OnBackClicked) },
            actions = {
                if (uiState is PlayerUiState.Loaded) {
                    TopBarAction(
                        icon = TuneScoutIcons.moreMenu,
                        contentDescription = stringResource(ComponentR.string.ui_more_options),
                        onClick = { onEvent(PlayerUiEvent.OnMoreClicked) },
                    )
                }
            },
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val artworkSize = getArtworkSize(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                isSideBySide = isSideBySide,
            )
            when (uiState) {
                PlayerUiState.Loading -> PlayerSkeleton(
                    isSideBySide = isSideBySide,
                    artworkSize = artworkSize,
                    artworkTopSpacing = getArtworkTopSpacing(maxHeight = maxHeight, artworkSize = artworkSize),
                    artworkCornerPercent = ARTWORK_CORNER_PERCENT,
                )

                PlayerUiState.NotFound -> StateMessage(
                    title = stringResource(R.string.player_not_found_title),
                    description = stringResource(R.string.player_not_found_description),
                )

                is PlayerUiState.Loaded -> if (isSideBySide) {
                    SideBySideContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        artworkSize = artworkSize,
                    )
                } else {
                    StackedContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        artworkSize = artworkSize,
                        artworkTopSpacing = getArtworkTopSpacing(maxHeight = maxHeight, artworkSize = artworkSize),
                    )
                }
            }
        }
    }
}

private fun getArtworkSize(
    maxWidth: Dp,
    maxHeight: Dp,
    isSideBySide: Boolean,
): Dp = when {
    isSideBySide -> minOf(maxHeight - TuneScoutSpacing.medium, maxWidth / 2)
    else -> minOf(maxWidth - TuneScoutSpacing.large * 2, maxHeight - detailsHeight)
}.coerceIn(minArtworkSize, maxArtworkSize)

private fun getArtworkTopSpacing(
    maxHeight: Dp,
    artworkSize: Dp,
): Dp = ((maxHeight - detailsHeight - artworkSize) / 2).coerceIn(0.dp, maxArtworkTopSpacing)

@Composable
private fun StackedContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    artworkSize: Dp,
    artworkTopSpacing: Dp,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(artworkTopSpacing))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            SongArtwork(uiState = uiState, size = artworkSize)
        }
        Spacer(modifier = Modifier.weight(1f))
        PlayerDetails(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.padding(
                horizontal = TuneScoutSpacing.large,
                vertical = TuneScoutSpacing.medium,
            ),
        )
    }
}

@Composable
private fun SideBySideContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    artworkSize: Dp,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArtwork(uiState = uiState, size = artworkSize)
        PlayerDetails(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SongArtwork(
    uiState: PlayerUiState.Loaded,
    size: Dp,
) {
    Artwork(
        url = uiState.song.artwork.largeUrl,
        contentDescription = stringResource(ComponentR.string.ui_artwork_of, uiState.song.albumTitle),
        cornerPercent = ARTWORK_CORNER_PERCENT,
        sharedKey = getSongSharedKey(uiState.song.id, SongSharedElement.ARTWORK),
        modifier = Modifier.size(size),
    )
}

@Composable
private fun PlayerDetails(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
    ) {
        SongHeading(
            songId = uiState.song.id,
            title = uiState.song.title,
            artistName = uiState.song.artistName,
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
            playButtonState = getPlayButtonState(isPlaying = uiState.isPlaying, hasEnded = uiState.hasEnded),
            hasPrevious = uiState.hasPrevious,
            hasNext = uiState.hasNext,
            isRepeatEnabled = uiState.isRepeatEnabled,
            onPlayPauseClick = { onEvent(PlayerUiEvent.OnPlayPauseClicked) },
            onPreviousClick = { onEvent(PlayerUiEvent.OnSkipPreviousClicked) },
            onNextClick = { onEvent(PlayerUiEvent.OnSkipNextClicked) },
            onRepeatClick = { onEvent(PlayerUiEvent.OnRepeatClicked) },
            onQueueClick = { onEvent(PlayerUiEvent.OnQueueClicked) },
        )
    }
}
