package com.pierre.tunescout.ui.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pierre.tunescout.ui.theme.TuneScoutColors

/**
 * A [Switch] whose "on" wears the accent. Material paints a checked switch with `primary`, which the
 * app maps to the text colour, so a plain [Switch] reads as black or white either way. With dynamic
 * colours on, the accent is the system's own primary, so turning that very switch on shows the
 * colour it brings in.
 *
 * @param onCheckedChange null when the row around the switch is what toggles it.
 */
@Composable
fun SwitchToggle(
    isChecked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = TuneScoutColors.accent,
            checkedThumbColor = TuneScoutColors.background,
            checkedIconColor = TuneScoutColors.accent,
        ),
    )
}
