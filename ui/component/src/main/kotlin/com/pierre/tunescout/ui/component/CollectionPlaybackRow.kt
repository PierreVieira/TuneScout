package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val playButtonSize = 56.dp
private val playIconSize = 36.dp
private val pauseIconSize = 28.dp

/**
 * The shuffle toggle and the play button a whole collection — an album, a playlist — is started
 * from, laid out the way Spotify lays them out: at the end of the row, the play button last and
 * filled with the accent.
 *
 * @param isPlaying whether the collection is what the player is playing, which turns the play
 * button into a pause button.
 * @param playContentDescription what the play button says it does while [isPlaying] is false.
 */
@Composable
fun CollectionPlaybackRow(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    playContentDescription: String,
    onPlayPauseClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShuffleButton(isEnabled = isShuffleEnabled, onClick = onShuffleClick)
        Box(
            modifier = Modifier
                .size(playButtonSize)
                .clip(CircleShape)
                .background(TuneScoutColors.accent)
                .clickable(onClick = onPlayPauseClick, role = Role.Button),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) TuneScoutIcons.pause else TuneScoutIcons.play,
                contentDescription = if (isPlaying) stringResource(R.string.ui_pause) else playContentDescription,
                tint = TuneScoutColors.background,
                modifier = Modifier.size(if (isPlaying) pauseIconSize else playIconSize),
            )
        }
    }
}
