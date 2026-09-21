package com.pierre.tunescout.ui.component

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
private val actionButtonSize = 48.dp
private val actionIconSize = 20.dp
private val unavailableIconSize = 14.dp
private const val UNAVAILABLE_ALPHA = 0.38f

/**
 * A row for one song: its artwork, its title and its artist, and whatever a screen puts at its end.
 *
 * A song the player cannot reach — offline, one whose preview never reached the device — has that
 * part of the row drawn faded, so a tap that is refused is seen coming. Only the song itself fades:
 * the row still answers taps, and the actions at its end work as they always do.
 *
 * Fading and the accent on a paused song are colour alone, so the row also says them: an offline
 * glyph beside the artist of a song that cannot play, and a state a screen reader reads with the row.
 *
 * @param onClickLabel what a tap on the row does, read by a screen reader in place of "activate";
 * null for a row whose tap does not play the song.
 */
@Composable
fun SongRow(
    title: String,
    subtitle: String,
    artworkUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = stringResource(R.string.ui_play),
    artworkSize: Dp = 52.dp,
    nowPlaying: NowPlayingState = NowPlayingState.None,
    isUnavailable: Boolean = false,
    sharedSongId: Long? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val isNowPlaying = nowPlaying != NowPlayingState.None
    val state = songStateDescription(nowPlaying = nowPlaying, isUnavailable = isUnavailable)
    CompositionLocalProvider(LocalSharedArtworkSurface provides SharedArtworkSurface.LIST_ROW) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(rowCornerRadius))
                .clickable(onClickLabel = onClickLabel, onClick = onClick)
                .semantics { if (state != null) stateDescription = state }
                .padding(vertical = TuneScoutSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isUnavailable) UNAVAILABLE_ALPHA else 1f),
                horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(
                    url = artworkUrl,
                    contentDescription = null,
                    cornerPercent = ARTWORK_CORNER_PERCENT,
                    sharedKey = SongSharedKey.createOrNull(sharedSongId, SongSharedElement.ARTWORK),
                    modifier = Modifier.size(artworkSize),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(visible = nowPlaying == NowPlayingState.Playing) {
                            NowPlayingBarsIcon(modifier = Modifier.padding(end = TuneScoutSpacing.small))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isNowPlaying) TuneScoutColors.accent else TuneScoutColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .sharedTextBounds(SongSharedKey.createOrNull(sharedSongId, SongSharedElement.TITLE)),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isUnavailable) {
                            Icon(
                                imageVector = TuneScoutIcons.offline,
                                contentDescription = null,
                                tint = TuneScoutColors.textSecondary,
                                modifier = Modifier.size(unavailableIconSize),
                            )
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isNowPlaying) TuneScoutColors.textEmphasis else TuneScoutColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.sharedTextBounds(
                                SongSharedKey.createOrNull(sharedSongId, SongSharedElement.ARTIST),
                            ),
                        )
                    }
                }
            }
            trailing()
        }
    }
}

/**
 * A playing song needs no state of its own: [NowPlayingBarsIcon] is on the row and says so.
 *
 * @return what the row's colours say about the song, in words, or null when they say nothing.
 */
@Composable
private fun songStateDescription(
    nowPlaying: NowPlayingState,
    isUnavailable: Boolean,
): String? = when {
    isUnavailable -> stringResource(R.string.ui_song_state_unavailable)
    nowPlaying == NowPlayingState.Paused -> stringResource(R.string.ui_song_state_paused)
    else -> null
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
            tint = TuneScoutColors.textTertiary,
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
