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
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
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
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.semantics.screenPane
import com.pierre.tunescout.ui.component.R as ComponentR

private val maxArtworkSize = 264.dp
private val minArtworkSize = 120.dp
private val compactArtworkSize = 64.dp
private val maxArtworkTopSpacing = 100.dp
private const val ARTWORK_CORNER_PERCENT = 12
private val detailsControlsHeight = 170.dp
private val detailsTextHeight = 90.dp

@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    layout: PlayerLayout,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    hasBack: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .screenPane(stringResource(R.string.player_now_playing))
            .safeDrawingPadding(),
    ) {
        TopBar(
            title = stringResource(R.string.player_now_playing),
            onBackClick = if (hasBack) {
                { onEvent(PlayerUiEvent.OnBackClicked) }
            } else {
                null
            },
            actions = {
                if (uiState is PlayerUiState.Loaded) {
                    FavoriteAction(isFavorite = uiState.isFavorite, onEvent = onEvent)
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
                layout = layout,
            )
            val artworkTopSpacing = getArtworkTopSpacing(
                maxHeight = maxHeight,
                detailsHeight = detailsHeight,
                artworkSize = artworkSize,
            )
            when (uiState) {
                PlayerUiState.Loading -> PlayerSkeleton(
                    layout = layout,
                    artworkSize = artworkSize,
                    artworkTopSpacing = artworkTopSpacing,
                    artworkCornerPercent = ARTWORK_CORNER_PERCENT,
                )

                PlayerUiState.NotFound -> StateMessage(
                    title = stringResource(R.string.player_not_found_title),
                    description = stringResource(R.string.player_not_found_description),
                )

                PlayerUiState.NothingPlaying -> StateMessage(
                    title = stringResource(R.string.player_nothing_playing_title),
                    description = stringResource(R.string.player_nothing_playing_description),
                )

                is PlayerUiState.Loaded -> when (layout) {
                    PlayerLayout.Stacked -> StackedContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        artworkSize = artworkSize,
                        artworkTopSpacing = artworkTopSpacing,
                        viewportHeight = maxHeight,
                    )

                    PlayerLayout.SideBySide -> SideBySideContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        artworkSize = artworkSize,
                    )

                    PlayerLayout.Compact -> CompactContent(
                        uiState = uiState,
                        onEvent = onEvent,
                        artworkSize = artworkSize,
                        viewportHeight = maxHeight,
                    )
                }
            }
        }
    }
}

/**
 * Liking is a state, so it stays on the bar where a filled heart can show it, beside the overflow
 * instead of behind it.
 */
@Composable
private fun FavoriteAction(
    isFavorite: Boolean,
    onEvent: (PlayerUiEvent) -> Unit,
) {
    TopBarAction(
        icon = if (isFavorite) TuneScoutIcons.favoriteFilled else TuneScoutIcons.favorite,
        contentDescription = stringResource(
            if (isFavorite) ComponentR.string.ui_unfavorite else ComponentR.string.ui_favorite,
        ),
        tint = if (isFavorite) TuneScoutColors.accent else TuneScoutColors.textPrimary,
        onClick = { onEvent(PlayerUiEvent.OnFavoriteClicked) },
    )
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
    layout: PlayerLayout,
): Dp = when (layout) {
    PlayerLayout.Stacked -> minOf(maxWidth - TuneScoutSpacing.large * 2, maxHeight - detailsHeight)
        .coerceIn(minArtworkSize, maxArtworkSize)

    PlayerLayout.SideBySide -> minOf(maxHeight - TuneScoutSpacing.medium, maxWidth / 2)
        .coerceIn(minArtworkSize, maxArtworkSize)

    PlayerLayout.Compact -> compactArtworkSize
}

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

/**
 * The artwork shrinks to a thumbnail beside the title, so the timeline and the controls under them
 * fit a pane as short as half a phone on its side. The padding and the gaps are tighter too: at 800dp
 * the pane is some 330dp wide inside, and the six controls only share one row with this much room.
 * The column is centred in the [viewportHeight], and scrolls when even that does not fit — the
 * largest font.
 */
@Composable
private fun CompactContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    artworkSize: Dp,
    viewportHeight: Dp,
) {
    PlayerDetailsContent(
        uiState = uiState,
        onEvent = onEvent,
        headingArtworkSize = artworkSize,
        spacing = TuneScoutSpacing.medium,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .heightIn(min = viewportHeight)
            .padding(horizontal = TuneScoutSpacing.medium, vertical = TuneScoutSpacing.small),
    )
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

/**
 * @param headingArtworkSize the size of the artwork drawn beside the heading, or null when the
 * artwork is drawn elsewhere.
 * @param spacing the gap between the heading, the timeline and the controls.
 */
@Composable
private fun PlayerDetailsContent(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    headingArtworkSize: Dp? = null,
    spacing: Dp = TuneScoutSpacing.screen,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (headingArtworkSize != null) {
                SongArtwork(uiState = uiState, size = headingArtworkSize)
            }
            SongHeading(
                songId = uiState.song.id,
                title = uiState.song.title,
                artistName = uiState.song.artistName,
                modifier = Modifier.weight(1f),
            )
        }
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
