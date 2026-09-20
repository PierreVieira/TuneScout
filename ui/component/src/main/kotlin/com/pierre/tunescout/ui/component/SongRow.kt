package com.pierre.tunescout.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.animation.LocalSharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.SharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.sharedTextBounds

private val rowCornerRadius = 8.dp
private const val ARTWORK_CORNER_PERCENT = 15
private val actionButtonSize = 36.dp
private val actionIconSize = 20.dp

@Composable
fun SongRow(
    title: String,
    subtitle: String,
    artworkUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artworkSize: Dp = 52.dp,
    nowPlaying: NowPlayingState = NowPlayingState.None,
    sharedSongId: Long? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val isNowPlaying = nowPlaying != NowPlayingState.None
    CompositionLocalProvider(LocalSharedArtworkSurface provides SharedArtworkSurface.LIST_ROW) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(rowCornerRadius))
                .clickable(onClick = onClick)
                .padding(vertical = TuneScoutSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(
                    url = artworkUrl,
                    contentDescription = null,
                    cornerPercent = ARTWORK_CORNER_PERCENT,
                    sharedKey = getSongSharedKey(sharedSongId, SongSharedElement.ARTWORK),
                    modifier = Modifier.size(artworkSize),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NowPlayingBars(state = nowPlaying)
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isNowPlaying) TuneScoutColors.accent else TuneScoutColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .sharedTextBounds(getSongSharedKey(sharedSongId, SongSharedElement.TITLE)),
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNowPlaying) TuneScoutColors.textEmphasis else TuneScoutColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedTextBounds(
                            getSongSharedKey(sharedSongId, SongSharedElement.ARTIST),
                        ),
                    )
                }
            }
            trailing()
        }
    }
}

@Composable
fun SongRowAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(actionButtonSize),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TuneScoutColors.elementMuted,
            modifier = Modifier.size(actionIconSize),
        )
    }
}

@Composable
fun SongRowMoreAction(onClick: () -> Unit) {
    SongRowAction(
        icon = TuneScoutIcons.moreMenu,
        contentDescription = stringResource(R.string.ui_more_options),
        onClick = onClick,
    )
}
