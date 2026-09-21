package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val targetSize = 48.dp
private val iconSize = 24.dp
private val dotSize = 4.dp
private val dotBottomPadding = 5.dp

/**
 * The icon of a playback mode — shuffle, repeat — inside a full touch target, with a dot under it
 * while the mode [isOn]. The dot is what tells the state apart for someone who cannot tell the two
 * tints apart: repeating the queue and not repeating draw the same glyph.
 *
 * It draws and nothing else. Whoever uses it says through [modifier] what a tap does and how the
 * state is announced, since a two-state mode is a switch and a three-state one is not.
 */
@Composable
fun PlaybackModeIcon(
    icon: ImageVector,
    contentDescription: String,
    isOn: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = if (isOn) TuneScoutColors.textPrimary else TuneScoutColors.textTertiary
    Box(
        modifier = modifier.size(targetSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
        if (isOn) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = dotBottomPadding)
                    .size(dotSize)
                    .background(tint, CircleShape),
            )
        }
    }
}
