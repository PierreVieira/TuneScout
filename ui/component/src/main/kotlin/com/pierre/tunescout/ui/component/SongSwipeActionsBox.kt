package com.pierre.tunescout.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import kotlin.math.abs
import kotlin.math.roundToInt

private val rowCornerRadius = 8.dp
private val actionIconSize = 24.dp
private val triggerDistance = 96.dp
private const val MAX_DRAG_FRACTION = 1.5f

/**
 * A song row that answers a sideways swipe the way a music app's does: toward the end it adds the
 * song to the queue, toward the start it likes the song — or takes the like back. The row follows
 * the finger, the action shows behind it and lights up once letting go would trigger it, and the
 * row always springs back into place: nothing leaves the list.
 *
 * Both actions are also offered to accessibility services, so they do not depend on the gesture.
 *
 * @param isEnabled whether the row answers the swipe at all. A list being reordered turns it off: the
 * row is there to be moved then, and neither action is offered — not even to accessibility services,
 * so the ones for moving the row are all that is read.
 */
@Composable
fun SongSwipeActionsBox(
    isFavorite: Boolean,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val triggerPx = with(LocalDensity.current) { triggerDistance.toPx() }
    val maxDragPx = triggerPx * MAX_DRAG_FRACTION
    val haptics = LocalHapticFeedback.current
    val currentOnAddToQueue by rememberUpdatedState(onAddToQueue)
    val currentOnToggleFavorite by rememberUpdatedState(onToggleFavorite)
    var offset by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        val next = (offset + delta).coerceIn(-maxDragPx, maxDragPx)
        if ((abs(offset) >= triggerPx) != (abs(next) >= triggerPx)) {
            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
        offset = next
    }
    val queueLabel = stringResource(R.string.ui_add_to_queue)
    val favoriteLabel = stringResource(if (isFavorite) R.string.ui_unfavorite else R.string.ui_favorite)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(rowCornerRadius))
            .semantics {
                if (isEnabled) {
                    customActions = listOf(
                        CustomAccessibilityAction(queueLabel) { true.also { currentOnAddToQueue() } },
                        CustomAccessibilityAction(favoriteLabel) { true.also { currentOnToggleFavorite() } },
                    )
                }
            },
    ) {
        val dragged = offset
        if (dragged != 0f) {
            SwipeActionBackgroundBox(
                icon = when {
                    dragged > 0f -> TuneScoutIcons.addToQueue
                    isFavorite -> TuneScoutIcons.favoriteFilled
                    else -> TuneScoutIcons.favorite
                },
                alignment = if (dragged > 0f) Alignment.CenterStart else Alignment.CenterEnd,
                isArmed = abs(dragged) >= triggerPx,
                modifier = Modifier.matchParentSize(),
            )
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .background(TuneScoutColors.background)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    enabled = isEnabled,
                    reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl,
                    onDragStopped = {
                        when {
                            offset >= triggerPx -> currentOnAddToQueue()
                            offset <= -triggerPx -> currentOnToggleFavorite()
                        }
                        animate(initialValue = offset, targetValue = 0f) { value, _ -> offset = value }
                    },
                ),
        ) {
            content()
        }
    }
}

@Composable
private fun SwipeActionBackgroundBox(
    icon: ImageVector,
    alignment: Alignment,
    isArmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (isArmed) TuneScoutColors.accentContainer else TuneScoutColors.surfaceRaised,
        label = "swipeActionBackground",
    )
    Box(
        modifier = modifier
            .background(color)
            .padding(horizontal = TuneScoutSpacing.large),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isArmed) TuneScoutColors.textPrimary else TuneScoutColors.textTertiary,
            modifier = Modifier.size(actionIconSize),
        )
    }
}
