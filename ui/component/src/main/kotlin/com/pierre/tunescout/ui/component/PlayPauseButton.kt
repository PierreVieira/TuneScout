package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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

private val buttonSize = 72.dp
private val playIconSize = 54.dp
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
                imageVector = TuneScoutIcons.pause,
                contentDescription = stringResource(R.string.ui_pause),
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier.size(pauseIconSize),
            )
        } else {
            Icon(
                imageVector = TuneScoutIcons.play,
                contentDescription = stringResource(R.string.ui_play),
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier.size(playIconSize),
            )
        }
    }
}
