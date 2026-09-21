package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.miniplayer.R
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiEvent
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.SongSharedElement
import com.pierre.tunescout.ui.component.SongSharedKey
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.component.contentDescription
import com.pierre.tunescout.ui.component.icon
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.animation.LocalSharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.LocalTappedSharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.SharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.loopingMarquee
import com.pierre.tunescout.ui.utils.animation.sharedTextBounds

private val cardCornerRadius = 12.dp
private const val ARTWORK_CORNER_PERCENT = 18
private val artworkSize = 44.dp
private val buttonSize = 48.dp
private val iconSize = 24.dp
private val progressHeight = 2.dp

@Composable
fun MiniPlayerContent(
    song: Song,
    playButtonState: PlayButtonState,
    progress: Float,
    onEvent: (MiniPlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openLabel = stringResource(R.string.mini_player_open)
    val tappedSurface = LocalTappedSharedArtworkSurface.current
    CompositionLocalProvider(LocalSharedArtworkSurface provides SharedArtworkSurface.MINI_PLAYER) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = TuneScoutSpacing.small, vertical = TuneScoutSpacing.extraSmall)
                .clip(RoundedCornerShape(cardCornerRadius))
                .background(TuneScoutColors.surfaceSubtle)
                .clickable(onClickLabel = openLabel) {
                    tappedSurface.surface = SharedArtworkSurface.MINI_PLAYER
                    onEvent(MiniPlayerUiEvent.OnClicked)
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TuneScoutSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(
                    url = song.artwork.thumbnailUrl,
                    contentDescription = null,
                    cornerPercent = ARTWORK_CORNER_PERCENT,
                    sharedKey = SongSharedKey.createOrNull(song.id, SongSharedElement.ARTWORK),
                    modifier = Modifier.size(artworkSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TuneScoutColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .sharedTextBounds(SongSharedKey.createOrNull(song.id, SongSharedElement.TITLE))
                            .loopingMarquee(),
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TuneScoutColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedTextBounds(
                            SongSharedKey.createOrNull(song.id, SongSharedElement.ARTIST),
                        ),
                    )
                }
                IconButton(
                    onClick = { onEvent(MiniPlayerUiEvent.OnQueueClicked) },
                    modifier = Modifier.size(buttonSize),
                ) {
                    Icon(
                        imageVector = TuneScoutIcons.musicList,
                        contentDescription = stringResource(R.string.mini_player_open_queue),
                        tint = TuneScoutColors.textPrimary,
                        modifier = Modifier.size(iconSize),
                    )
                }
                IconButton(
                    onClick = { onEvent(MiniPlayerUiEvent.OnPlayPauseClicked) },
                    modifier = Modifier.size(buttonSize),
                ) {
                    Icon(
                        imageVector = playButtonState.icon,
                        contentDescription = stringResource(playButtonState.contentDescription),
                        tint = TuneScoutColors.textPrimary,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
            ProgressLine(progress = progress)
        }
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    val animated by animateFloatAsState(targetValue = progress, label = "miniPlayerProgress")
    val trackColor = TuneScoutColors.surfaceSubtle
    val playedColor = TuneScoutColors.trackActive
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(progressHeight)
            .drawBehind {
                drawRect(color = trackColor)
                drawRect(
                    color = playedColor,
                    size = size.copy(width = size.width * animated.coerceIn(0f, 1f)),
                )
            },
    )
}
