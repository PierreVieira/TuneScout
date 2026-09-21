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
import coil3.SingletonImageLoader
import com.pierre.tunescout.screenshotfixtures.createArtworkImageLoader
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Before

private const val STATUS_BAR_CLOCK = "9:41"
private val mockupElevation = 32.dp
private val canvasGradientStart = Color(0xFF2C5766)
private val canvasGradientEnd = Color(0xFF081217)

internal abstract class ReadmeScreenshotsTest :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(
            mockupElevation = mockupElevation,
            edgeToEdge = false,
            statusBarClock = STATUS_BAR_CLOCK,
            background = { CanvasBackgroundBox() },
        ),
    ) {
    /** Pinned: the generators run under Robolectric, whose system theme is light. */
    private val pinnedTheme = Theme.DARK

    @Before
    fun setUpArtwork() {
        SingletonImageLoader.setSafe(::createArtworkImageLoader)
    }

    fun capture(
        fileName: String,
        title: String,
        description: String,
        content: @Composable () -> Unit,
    ) {
        screenshot(
            title = title,
            description = description,
            fileName = fileName,
            subdir = README_SUBDIR,
        ) {
            TuneScoutTheme(theme = pinnedTheme, content = content)
        }
    }

    private companion object {
        const val README_SUBDIR = "readme"
    }
}

@Composable
private fun CanvasBackgroundBox() {
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
