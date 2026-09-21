package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val buttonSize = 48.dp
private val iconSize = 24.dp

@Composable
fun ShuffleButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
    ) {
        Icon(
            imageVector = TuneScoutIcons.shuffle,
            contentDescription = stringResource(if (isEnabled) R.string.ui_shuffle_on else R.string.ui_shuffle_off),
            tint = if (isEnabled) TuneScoutColors.textPrimary else TuneScoutColors.elementSubtle,
            modifier = Modifier.size(iconSize),
        )
    }
}
