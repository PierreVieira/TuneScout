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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.player.R
import com.pierre.tunescout.feature.player.presentation.component.PlaybackControlsComponent
import com.pierre.tunescout.feature.player.presentation.component.PlaybackTimelineComponent
import com.pierre.tunescout.feature.player.presentation.component.PlayerSkeleton
import com.pierre.tunescout.feature.player.presentation.component.SongHeading
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.SongSharedElement
import com.pierre.tunescout.ui.component.SongSharedKey
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.semantics.screenPane
import com.pierre.tunescout.ui.component.R as ComponentR

private val maxArtworkSize = 264.dp
private val minArtworkSize = 120.dp
private val maxArtworkTopSpacing = 100.dp
private const val ARTWORK_CORNER_PERCENT = 12
private val detailsControlsHeight = 170.dp
private val detailsTextHeight = 90.dp

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
            .screenPane(stringResource(R.string.player_now_playing))
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
            val detailsHeight = detailsHeight()
            val artworkSize = getArtworkSize(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                detailsHeight = detailsHeight,
                isSideBySide = isSideBySide,
            )
            val artworkTopSpacing = getArtworkTopSpacing(
                maxHeight = maxHeight,
                detailsHeight = detailsHeight,
                artworkSize = artworkSize,
            )
            when (uiState) {
                PlayerUiState.Loading -> PlayerSkeleton(
                    isSideBySide = isSideBySide,
                    artworkSize = artworkSize,
                    artworkTopSpacing = artworkTopSpacing,
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
                        artworkTopSpacing = artworkTopSpacing,
                        viewportHeight = maxHeight,
                    )
                }
            }
        }
    }
}

/**
 * The details are controls, which keep their size, and three lines of text, which grow with the
 * font. Sizing the artwork against a fixed height left the text no room at a large font scale: the
 * cover kept its share and the controls under the text were pushed off the screen.
 *
 * @return the height to keep free under the artwork at the font scale in force.
 */
@Composable
private fun detailsHeight(): Dp = detailsControlsHeight + detailsTextHeight * LocalDensity.current.fontScale

private fun getArtworkSize(
    maxWidth: Dp,
    maxHeight: Dp,
    detailsHeight: Dp,
    isSideBySide: Boolean,
): Dp = when {
    isSideBySide -> minOf(maxHeight - TuneScoutSpacing.medium, maxWidth / 2)
    else -> minOf(maxWidth - TuneScoutSpacing.large * 2, maxHeight - detailsHeight)
}.coerceIn(minArtworkSize, maxArtworkSize)

private fun getArtworkTopSpacing(
    maxHeight: Dp,
    detailsHeight: Dp,
    artworkSize: Dp,
): Dp = ((maxHeight - detailsHeight - artworkSize) / 2).coerceIn(0.dp, maxArtworkTopSpacing)

/**
 * The column is at least as tall as the [viewportHeight], which is what lets the weighted spacer
 * push the details to the bottom, and it scrolls: when the smallest artwork and the details still
 * do not fit — a short window, the largest font — the controls are a scroll away instead of cut off.
 */
@Composable
private fun StackedContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    artworkSize: Dp,
    artworkTopSpacing: Dp,
    viewportHeight: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .heightIn(min = viewportHeight),
    ) {
        Spacer(modifier = Modifier.height(artworkTopSpacing))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            SongArtwork(uiState = uiState, size = artworkSize)
        }
        Spacer(modifier = Modifier.weight(1f))
        PlayerDetailsContent(
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
        PlayerDetailsContent(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
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
        sharedKey = SongSharedKey.createOrNull(uiState.song.id, SongSharedElement.ARTWORK),
        modifier = Modifier.size(size),
    )
}

@Composable
private fun PlayerDetailsContent(
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
        PlaybackTimelineComponent(
            songId = uiState.song.id,
            progress = uiState.progress,
            position = uiState.position,
            duration = uiState.duration,
            onSeekFinished = { fraction ->
                onEvent(PlayerUiEvent.OnSeekFinished(uiState.duration * fraction.toDouble()))
            },
        )
        PlaybackControlsComponent(
            playButtonState = PlayButtonState.of(isPlaying = uiState.isPlaying, hasEnded = uiState.hasEnded),
            hasPrevious = uiState.hasPrevious,
            hasNext = uiState.hasNext,
            repeatMode = uiState.repeatMode,
            isShuffleEnabled = uiState.isShuffleEnabled,
            onPlayPauseClick = { onEvent(PlayerUiEvent.OnPlayPauseClicked) },
            onPreviousClick = { onEvent(PlayerUiEvent.OnSkipPreviousClicked) },
            onNextClick = { onEvent(PlayerUiEvent.OnSkipNextClicked) },
            onRepeatClick = { onEvent(PlayerUiEvent.OnRepeatClicked) },
            onShuffleClick = { onEvent(PlayerUiEvent.OnShuffleClicked) },
            onQueueClick = { onEvent(PlayerUiEvent.OnQueueClicked) },
        )
    }
}
