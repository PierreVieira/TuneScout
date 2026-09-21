package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * How much of the bottom the keyboard takes, read straight from the window: the screens that show a
 * snackbar sit inside a host that has already consumed the IME inset, so [safeDrawingPadding] alone
 * would leave the snackbar underneath the keyboard.
 */
private val imeHeight: Dp
    @Composable get() = WindowInsets.ime
        .asPaddingValues()
        .calculateBottomPadding()

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
    Box(modifier = modifier) {
        content()
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = imeHeight)
                .safeDrawingPadding(),
        )
    }
}
