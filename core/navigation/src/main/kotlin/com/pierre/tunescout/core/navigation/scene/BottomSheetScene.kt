package com.pierre.tunescout.core.navigation.scene

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.window.rememberWindowSize

private const val DIALOG_WIDTH_FRACTION = 0.9f
private const val DIALOG_HEIGHT_FRACTION = 0.9f
private val dialogMaxWidth = 560.dp
private val dialogCornerRadius = 16.dp
private val closeButtonSize = 40.dp
private val closeIconSize = 20.dp

/**
 * A sheet, or a dialog in a window too short to open one. A landscape phone leaves a bottom sheet
 * barely a row of content between the drag handle and the navigation bar, so the same entry is
 * centred as a dialog there instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val containerColor: @Composable () -> Color,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val lifecycleOwner = rememberLifecycleOwner()
        val entryContent: @Composable () -> Unit = {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                entry.Content()
            }
        }
        if (rememberWindowSize().isHeightCompact) {
            OverlayDialog(
                containerColor = containerColor(),
                onDismissRequest = onBack,
                content = entryContent,
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = onBack,
                // A landscape window is short enough that the half-open state hides the last option.
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = containerColor(),
                content = { entryContent() },
            )
        }
    }
}

@Composable
private fun OverlayDialog(
    containerColor: Color,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // The dialog window is the whole screen, so the card caps itself: without the cap a long
        // sheet runs off both edges instead of scrolling inside it, and tapping outside the card
        // would stop dismissing it if the card filled the window.
        val windowHeight = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.height
                .toDp()
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(DIALOG_WIDTH_FRACTION)
                .widthIn(max = dialogMaxWidth)
                .heightIn(max = windowHeight * DIALOG_HEIGHT_FRACTION),
            shape = RoundedCornerShape(dialogCornerRadius),
            color = containerColor,
        ) {
            // A centred dialog never touches the navigation bar, so the padding the contents take
            // for it as sheets is consumed here instead of doubling their own bottom padding.
            Column(modifier = Modifier.consumeWindowInsets(WindowInsets.navigationBars)) {
                // A dialog has no drag handle, so it says how it closes: the button is also the top
                // padding the sheet used to get from that handle.
                CloseButton(onClick = onDismissRequest)
                content()
            }
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .padding(end = TuneScoutSpacing.small)
                .size(closeButtonSize),
        ) {
            Icon(
                imageVector = TuneScoutIcons.clear,
                contentDescription = stringResource(R.string.ui_close),
                tint = TuneScoutColors.textSecondary,
                modifier = Modifier.size(closeIconSize),
            )
        }
    }
}
