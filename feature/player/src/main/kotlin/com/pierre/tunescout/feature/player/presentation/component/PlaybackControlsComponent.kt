package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.PlayPauseButton
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.ShuffleButton
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.feature.player.R as PlayerR

private val buttonSize = 48.dp
private val skipIconSize = 44.dp
private val repeatIconSize = 24.dp

@Composable
internal fun PlaybackControlsComponent(
    playButtonState: PlayButtonState,
    hasPrevious: Boolean,
    hasNext: Boolean,
    repeatMode: RepeatMode,
    isShuffleEnabled: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayPauseButton(
            state = playButtonState,
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
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShuffleButton(
                isEnabled = isShuffleEnabled,
                onClick = onShuffleClick,
            )
            RepeatButton(
                mode = repeatMode,
                onClick = onRepeatClick,
            )
            QueueButton(onClick = onQueueClick)
        }
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
            tint = if (enabled) TuneScoutColors.textPrimary else TuneScoutColors.elementSubtle,
            modifier = Modifier.size(skipIconSize),
        )
    }
}

@Composable
private fun RepeatButton(
    mode: RepeatMode,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(buttonSize),
    ) {
        Icon(
            imageVector = if (mode == RepeatMode.One) TuneScoutIcons.repeatOne else TuneScoutIcons.repeat,
            contentDescription = stringResource(
                when (mode) {
                    RepeatMode.Off -> R.string.ui_repeat_off
                    RepeatMode.All -> R.string.ui_repeat_all
                    RepeatMode.One -> R.string.ui_repeat_one
                },
            ),
            tint = if (mode == RepeatMode.Off) TuneScoutColors.elementSubtle else TuneScoutColors.textPrimary,
            modifier = Modifier.size(repeatIconSize),
        )
    }
}
