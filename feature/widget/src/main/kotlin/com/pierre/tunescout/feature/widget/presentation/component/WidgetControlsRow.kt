package com.pierre.tunescout.feature.widget.presentation.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.pierre.tunescout.feature.widget.R
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import com.pierre.tunescout.feature.widget.presentation.widget.SkipToNextAction
import com.pierre.tunescout.feature.widget.presentation.widget.SkipToPreviousAction
import com.pierre.tunescout.feature.widget.presentation.widget.TogglePlayPauseAction

private val controlIconSize = 24.dp
private val playIconSize = 30.dp
private val controlTargetSize = 48.dp

/**
 * Previous, play or pause, and next. A skip the queue cannot honour is drawn muted, the same way
 * the player's own controls behave — and, like them, it stops answering: `RemoteViews` has no
 * disabled state to announce, so the button loses its click and its description says it is
 * unavailable, instead of a screen reader offering a button that does nothing.
 *
 * Each button is a full touch target around a smaller glyph. Where a launcher gives the row less
 * height than that, the target is clipped and the glyph, centred in it, is not.
 */
@Composable
internal fun WidgetControlsRow(
    state: WidgetState,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WidgetControlButton(
            iconRes = R.drawable.ic_widget_skip_to_previous,
            contentDescriptionRes = R.string.widget_skip_to_previous,
            isEnabled = state.hasPrevious,
            size = controlIconSize,
            action = actionRunCallback<SkipToPreviousAction>(),
        )
        WidgetControlButton(
            iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
            contentDescriptionRes = if (state.isPlaying) R.string.widget_pause else R.string.widget_play,
            isEnabled = state.song != null,
            size = playIconSize,
            action = actionRunCallback<TogglePlayPauseAction>(),
        )
        WidgetControlButton(
            iconRes = R.drawable.ic_widget_skip_to_next,
            contentDescriptionRes = R.string.widget_skip_to_next,
            isEnabled = state.hasNext,
            size = controlIconSize,
            action = actionRunCallback<SkipToNextAction>(),
        )
    }
}

@Composable
private fun WidgetControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    isEnabled: Boolean,
    size: Dp,
    action: Action,
) {
    val context = LocalContext.current
    val label = context.getString(contentDescriptionRes)
    val target = GlanceModifier.size(controlTargetSize)
    Box(
        modifier = if (isEnabled) target.clickable(action) else target,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = if (isEnabled) {
                label
            } else {
                context.getString(
                    R.string.widget_control_unavailable,
                    label,
                )
            },
            modifier = GlanceModifier.size(size),
            colorFilter = ColorFilter.tint(getControlTint(isEnabled)),
        )
    }
}

private fun getControlTint(isEnabled: Boolean): ColorProvider =
    if (isEnabled) WidgetColors.element else WidgetColors.elementDisabled
