package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.PlayPauseButton
import com.pierre.tunescout.ui.component.PlaybackModeIcon
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.ShuffleButton
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.feature.player.R as PlayerR

private val buttonSize = 48.dp
private val regularMinWidth = 360.dp
private val regularPlayButtonSize = 72.dp
private val compactPlayButtonSize = 56.dp
private val skipIconSize = 44.dp
private val repeatIconSize = 24.dp

/**
 * The transport buttons at the start and the modes at the end, on one row wherever they fit.
 *
 * Six full touch targets and a large play button need [regularMinWidth]; a 360dp phone leaves 312dp
 * after the screen's padding, where the last button used to be squeezed out of its size. Under that
 * width the play button and the gaps shrink, and on a window narrower still — a 320dp screen, or a
 * larger one with the display size turned up — the modes wrap under the transport rather than
 * giving up a target.
 */
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
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompact = maxWidth < regularMinWidth
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 0.dp else TuneScoutSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayPauseButton(
                    state = playButtonState,
                    onClick = onPlayPauseClick,
                    size = if (isCompact) compactPlayButtonSize else regularPlayButtonSize,
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
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
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

/**
 * Repeat has three states, so it is a button with a state rather than a switch: the label stays
 * "Repeat" and the state says which of the three it is on.
 */
@Composable
private fun RepeatButton(
    mode: RepeatMode,
    onClick: () -> Unit,
) {
    val state = stringResource(
        when (mode) {
            RepeatMode.Off -> R.string.ui_repeat_state_off
            RepeatMode.All -> R.string.ui_repeat_state_all
            RepeatMode.One -> R.string.ui_repeat_state_one
        },
    )
    PlaybackModeIcon(
        icon = if (mode == RepeatMode.One) TuneScoutIcons.repeatOne else TuneScoutIcons.repeat,
        contentDescription = stringResource(R.string.ui_repeat),
        isOn = mode != RepeatMode.Off,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = state },
    )
}
