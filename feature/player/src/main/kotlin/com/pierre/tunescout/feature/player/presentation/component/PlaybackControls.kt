package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.PlayPauseButton
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.feature.player.R as PlayerR

private val buttonSize = 48.dp
private val skipIconSize = 44.dp
private val repeatIconSize = 24.dp

@Composable
internal fun PlaybackControls(
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    isRepeatEnabled: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick,
        )
        SkipButton(
            icon = TuneScoutIcons.skipPrevious,
            contentDescription = stringResource(R.string.ui_skip_previous),
            enabled = hasPrevious,
            onClick = onPreviousClick,
        )
        SkipButton(
            icon = TuneScoutIcons.skipNext,
            contentDescription = stringResource(R.string.ui_skip_next),
            enabled = hasNext,
            onClick = onNextClick,
        )
        Spacer(modifier = Modifier.weight(1f))
        RepeatButton(
            isEnabled = isRepeatEnabled,
            onClick = onRepeatClick,
        )
        QueueButton(onClick = onQueueClick)
    }
}

@Composable
private fun QueueButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(buttonSize),
    ) {
        Icon(
            imageVector = TuneScoutIcons.musicList,
            contentDescription = stringResource(PlayerR.string.player_open_queue),
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(repeatIconSize),
        )
    }
}

@Composable
private fun SkipButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(buttonSize),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) TuneScoutColors.textPrimary else TuneScoutColors.white25,
            modifier = Modifier.size(skipIconSize),
        )
    }
}

@Composable
private fun RepeatButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(buttonSize),
    ) {
        Icon(
            imageVector = TuneScoutIcons.repeat,
            contentDescription = stringResource(
                if (isEnabled) R.string.ui_repeat_on else R.string.ui_repeat_off,
            ),
            tint = if (isEnabled) TuneScoutColors.textPrimary else TuneScoutColors.white25,
            modifier = Modifier.size(repeatIconSize),
        )
    }
}
