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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val defaultButtonSize = 72.dp
private const val PLAY_ICON_FRACTION = 0.75f
private const val PAUSE_ICON_FRACTION = 0.39f
private const val REPLAY_ICON_FRACTION = 0.47f

/**
 * @param size the diameter of the button. Each glyph is a fraction of it, since the three do not
 * fill their frame alike: a play triangle drawn at the size of a pause would look small.
 */
@Composable
fun PlayPauseButton(
    state: PlayButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = defaultButtonSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(TuneScoutColors.surfaceRaised)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = state.icon,
            contentDescription = stringResource(state.contentDescription),
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(
                size * when (state) {
                    PlayButtonState.Play -> PLAY_ICON_FRACTION
                    PlayButtonState.Pause -> PAUSE_ICON_FRACTION
                    PlayButtonState.Replay -> REPLAY_ICON_FRACTION
                },
            ),
        )
    }
}
