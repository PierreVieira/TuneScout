package com.pierre.tunescout.screenshots

import androidx.compose.runtime.Composable
import coil3.SingletonImageLoader
import com.pierre.tunescout.screenshotfixtures.createArtworkImageLoader
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Before

internal abstract class ReadmeScreenshotsTest :
    StoreScreenshotsTest(
        formFactor = FormFactor.Phone,
        style = ScreenshotStyle(
            mockupElevation = readmeMockupElevation,
            edgeToEdge = false,
            statusBarClock = README_STATUS_BAR_CLOCK,
            background = { ReadmeCanvasBackgroundBox() },
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
}
