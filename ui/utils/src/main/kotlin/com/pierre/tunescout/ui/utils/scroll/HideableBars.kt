package com.pierre.tunescout.ui.utils.scroll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val FULLY_VISIBLE = 0f
private const val FULLY_HIDDEN = 1f
private const val HIDDEN_FRACTION_LABEL = "bars_hidden_fraction"
private val toggleDistance = 12.dp

/**
 * Whether the bars that frame a list are on screen. One instance is provided for the whole app: the
 * navigation bar is drawn by the navigation suite and the header by whichever screen is on top, and
 * a single scroll has to move both.
 *
 * The mini player is not one of them. It is the only thing on screen that says what is playing, so
 * it stays where it is and follows the navigation bar down as the content grows into its room.
 */
@Stable
class HideableBarsState {
    var areBarsVisible: Boolean by mutableStateOf(true)
        private set

    fun show() {
        areBarsVisible = true
    }

    internal fun hide() {
        areBarsVisible = false
    }
}

/**
 * Defaults to an instance of its own so that every `*Content` composable keeps rendering under
 * `androidTest` and `:tools:screenshots`, where nothing provides one.
 */
val LocalHideableBarsState: ProvidableCompositionLocal<HideableBarsState> =
    staticCompositionLocalOf { HideableBarsState() }

@Composable
fun rememberHideableBarsState(): HideableBarsState = remember { HideableBarsState() }

/**
 * Marks the subtree whose scrolling moves the bars. Because the state is shared, the screen that
 * scrolled them away is rarely the screen that pays for it: the bars come back both as a list
 * arrives and as it leaves, so nothing ever opens missing its header.
 */
@Composable
fun Modifier.hidesBarsOnScroll(): Modifier {
    val state = LocalHideableBarsState.current
    val toggleDistancePx = with(LocalDensity.current) { toggleDistance.toPx() }
    val connection = remember(state, toggleDistancePx) {
        HideableBarsNestedScrollConnection(state = state, toggleDistance = toggleDistancePx)
    }
    DisposableEffect(state) {
        state.show()
        onDispose { state.show() }
    }
    return nestedScroll(connection)
}

/**
 * Collapses a header upwards as the bars leave. The height it reports shrinks along with the
 * animation, so the list below takes the room instead of a gap opening where the header was.
 */
@Composable
fun Modifier.hideableTopBar(): Modifier {
    val hiddenFraction = rememberHiddenFraction()
    return clipToBounds().layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val hiddenHeight = (placeable.height * hiddenFraction.value).roundToInt()
        layout(width = placeable.width, height = placeable.height - hiddenHeight) {
            placeable.place(x = 0, y = -hiddenHeight)
        }
    }
}

@Composable
private fun rememberHiddenFraction(): State<Float> = animateFloatAsState(
    targetValue = if (LocalHideableBarsState.current.areBarsVisible) FULLY_VISIBLE else FULLY_HIDDEN,
    label = HIDDEN_FRACTION_LABEL,
)
