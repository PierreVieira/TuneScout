package com.pierre.tunescout.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pierre.tunescout.core.utils.toClockString
import com.pierre.tunescout.feature.widget.domain.model.WidgetState

private val barHeight = 4.dp
private val barSpacing = 6.dp
private val clockFontSize = 10.sp

/**
 * How far into the song the player is: the clock on either side and the bar between them.
 *
 * The bar only reports, it does not seek. `RemoteViews` has no draggable view — a widget receives
 * clicks and nothing else — which is why the system draws the scrubber on the media notification
 * and a widget cannot.
 */
@Composable
internal fun WidgetProgressRow(
    state: WidgetState,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WidgetClockText(text = state.elapsed.toClockString())
        Spacer(modifier = GlanceModifier.width(barSpacing))
        LinearProgressIndicator(
            progress = state.progress,
            modifier = GlanceModifier.defaultWeight().height(barHeight).cornerRadius(barHeight),
            color = WidgetColors.accent,
            backgroundColor = WidgetColors.surface,
        )
        Spacer(modifier = GlanceModifier.width(barSpacing))
        WidgetClockText(text = state.total.toClockString())
    }
}

@Composable
private fun WidgetClockText(text: String) {
    Text(
        text = text,
        style = TextStyle(color = WidgetColors.textSecondary, fontSize = clockFontSize),
        maxLines = 1,
    )
}
