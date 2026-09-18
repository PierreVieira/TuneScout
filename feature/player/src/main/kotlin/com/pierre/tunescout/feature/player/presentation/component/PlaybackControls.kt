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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.PlayPauseButton
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val controlsGap = 28.dp
private val skipButtonSize = 48.dp
private val skipIconSize = 36.dp
private const val NEXT_ROTATION = 180f

@Composable
internal fun PlaybackControls(
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(controlsGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkipButton(
            contentDescription = stringResource(R.string.ui_skip_previous),
            enabled = hasPrevious,
            rotation = 0f,
            onClick = onPreviousClick,
        )
        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick,
        )
        SkipButton(
            contentDescription = stringResource(R.string.ui_skip_next),
            enabled = hasNext,
            rotation = NEXT_ROTATION,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun SkipButton(
    contentDescription: String,
    enabled: Boolean,
    rotation: Float,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(skipButtonSize),
    ) {
        Icon(
            painter = painterResource(TuneScoutIcons.skip),
            contentDescription = contentDescription,
            tint = if (enabled) TuneScoutColors.textPrimary else TuneScoutColors.white25,
            modifier = Modifier
                .size(skipIconSize)
                .rotate(rotation),
        )
    }
}
