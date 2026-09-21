package com.pierre.tunescout.screenshotfixtures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val sheetCornerRadius = 28.dp

/**
 * A bottom sheet drawn by hand over [screen]: the scrim, the sheet colour and the drag handle around
 * [sheet]. The real sheets are `ModalBottomSheet`s, which animate in from a separate window that a
 * Robolectric capture does not wait for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetOverScreen(
    screen: @Composable () -> Unit,
    sheet: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        screen()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BottomSheetDefaults.ScrimColor),
        )
        Surface(
            color = TuneScoutColors.sheet,
            shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomSheetDefaults.DragHandle()
                sheet()
            }
        }
    }
}
