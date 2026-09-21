package com.pierre.tunescout.ui.component

import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role

/**
 * Shuffle is a switch: the label stays "Shuffle" and the role carries whether it is on, so a screen
 * reader says the state the way it says every other switch's instead of reading it out of the name.
 */
@Composable
fun ShuffleButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaybackModeIcon(
        icon = TuneScoutIcons.shuffle,
        contentDescription = stringResource(R.string.ui_shuffle),
        isOn = isEnabled,
        modifier = modifier
            .clip(CircleShape)
            .toggleable(value = isEnabled, role = Role.Switch, onValueChange = { onClick() }),
    )
}
