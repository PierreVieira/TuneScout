package com.pierre.tunescout.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import com.pierre.tunescout.screenshotfixtures.createArtworkImageLoader
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupOrientation
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Before

private const val LANDSCAPE_WIDTH_DP = 1280
private const val LANDSCAPE_HEIGHT_DP = 800

/**
 * Base for the two-pane README shots: a tablet held in landscape, wide enough to cross the app's
 * 800dp two-pane breakpoint. [FormFactor.Tablet10]'s built-in frame only ever measures its content
 * portrait, so `capture()` from [ReadmeScreenshotsTest] can't trigger the list-detail layout no
 * matter what canvas it is given — [FormFactor.logicalSize] comes from the form factor's own fixed
 * qualifiers, not from a [ScreenshotCanvas] override. These shots build the banner and the device
 * bezel by hand instead: a landscape canvas so the whole shot is wide, and
 * [DeviceMockup]'s [MockupOrientation.Landscape], which swaps the frame's own width/height so
 * [content] is actually measured — and laid out — at tablet-landscape proportions.
 */
internal abstract class ReadmeTabletScreenshotsTest :
    StoreScreenshotsTest(
        formFactor = FormFactor.Tablet10,
        style = ScreenshotStyle(
            mockupElevation = readmeMockupElevation,
            edgeToEdge = false,
            statusBarClock = README_STATUS_BAR_CLOCK,
            background = { ReadmeCanvasBackgroundBox() },
        ),
        canvas = ScreenshotCanvas.dp(LANDSCAPE_WIDTH_DP, LANDSCAPE_HEIGHT_DP),
    ) {
    /** Pinned: the generators run under Robolectric, whose system theme is light. */
    private val pinnedTheme = Theme.DARK
    private val bannerHorizontalPadding = 48.dp
    private val bannerVerticalPadding = 56.dp
    private val titleFontSize = 36.sp
    private val descriptionFontSize = 18.sp

    @Before
    fun setUpArtwork() {
        SingletonImageLoader.setSafe(::createArtworkImageLoader)
    }

    fun captureTwoPane(
        fileName: String,
        title: String,
        description: String,
        content: @Composable () -> Unit,
    ) {
        customScreenshot(fileName = fileName, subdir = README_SUBDIR) {
            Box(modifier = Modifier.fillMaxSize()) {
                ReadmeCanvasBackgroundBox()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = bannerHorizontalPadding, vertical = bannerVerticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = descriptionFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DeviceMockup(
                            formFactor = FormFactor.Tablet10,
                            orientation = MockupOrientation.Landscape,
                            modifier = Modifier.fillMaxHeight(),
                            showStatusBar = true,
                            statusBarClock = README_STATUS_BAR_CLOCK,
                            edgeToEdge = false,
                            elevation = readmeMockupElevation,
                        ) {
                            TuneScoutTheme(theme = pinnedTheme, content = content)
                        }
                    }
                }
            }
        }
    }
}
