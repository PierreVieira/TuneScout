package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * How much of the keyboard covers a box whose bottom edge sits at [bottomInWindow].
 *
 * The screens that show a snackbar sit inside a host that ends above the window — the mini player
 * bar is below it — and that has already declared the IME inset handled. So neither the inset
 * itself nor what is left of it after that says how much of the keyboard is really in the way:
 * only the box's own place in the window does.
 *
 * @return the covered height, and zero while the keyboard is down or already below the box.
 */
@Composable
private fun keyboardOverlap(bottomInWindow: Int): Dp {
    val density = LocalDensity.current
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val covered = WindowInsets.ime.getBottom(density) - (windowHeight - bottomInWindow)
    return with(density) { covered.coerceAtLeast(0).toDp() }
}

/**
 * Lays the snackbars of [hostState] over the bottom of [content], clear of the system bars and of
 * the keyboard. The box takes the size of [content], so a screen fills the window and an options
 * sheet keeps its height.
 */
@Composable
fun SnackbarBox(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var bottomInWindow by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            bottomInWindow = coordinates.positionInWindow().y.roundToInt() + coordinates.size.height
        },
    ) {
        content()
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = keyboardOverlap(bottomInWindow))
                .safeDrawingPadding(),
        )
    }
}
