package com.pierre.tunescout.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared by [ReadmeScreenshotsTest] (phone) and [ReadmeTabletScreenshotsTest] (landscape tablet). */
internal const val README_SUBDIR = "readme"
internal const val README_STATUS_BAR_CLOCK = "9:41"
internal val readmeMockupElevation = 32.dp
private val canvasGradientStart = Color(0xFF2C5766)
private val canvasGradientEnd = Color(0xFF081217)

@Composable
internal fun ReadmeCanvasBackgroundBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(canvasGradientStart, canvasGradientEnd),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
    )
}
