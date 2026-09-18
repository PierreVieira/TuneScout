package com.quare.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quare.tunescout.ui.theme.TuneScoutColors

private val buttonSize = 72.dp
private val playIconWidth = 26.dp
private val playIconHeight = 29.dp
private val playIconOffset = 1.5.dp
private val pauseIconSize = 28.dp

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(TuneScoutColors.white20)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlaying) {
            Icon(
                painter = painterResource(TuneScoutIcons.pause),
                contentDescription = stringResource(R.string.ui_pause),
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier.size(pauseIconSize),
            )
        } else {
            Icon(
                painter = painterResource(TuneScoutIcons.play),
                contentDescription = stringResource(R.string.ui_play),
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier
                    .offset(x = playIconOffset)
                    .size(width = playIconWidth, height = playIconHeight),
            )
        }
    }
}
