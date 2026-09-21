package com.pierre.tunescout.screenshottests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import coil3.SingletonImageLoader
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityCheckOptions
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityChecker
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.checkRoboAccessibility
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.pierre.tunescout.screenshotfixtures.createArtworkImageLoader
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import org.junit.Before
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Base class of every screenshot test: a Pixel 7 under Robolectric's native graphics, with the
 * committed covers served to Coil in place of the network. Each [snapshot] renders a composable once
 * per [ScreenshotVariant] into `src/test/screenshots/<test class>/<name>_<variant>.png`.
 *
 * It renders on Android 16 rather than the compileSdk: booting 37 needs extra JDK flags, and nothing
 * the app draws differs between the two.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
internal abstract class ScreenshotTest {
    /**
     * Every capture is also run through the Accessibility Test Framework — the checks behind
     * Accessibility Scanner: touch target size, text and image contrast, missing or duplicated
     * labels. An error fails the test like a pixel difference does; warnings are only logged, since
     * they include judgement calls (a contrast measured over artwork) that no screen can settle.
     */
    private val accessibilityCheckOptions = RoborazziATFAccessibilityCheckOptions(
        checker = RoborazziATFAccessibilityChecker(preset = AccessibilityCheckPreset.LATEST),
        failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Error,
    )

    @Before
    fun setUpArtwork() {
        SingletonImageLoader.setSafe(::createArtworkImageLoader)
    }

    /**
     * @param name what the images are named after, unique within the test class.
     * @param variants the themes, languages and font scales to render [content] in.
     * @param isLandscape renders on the phone turned sideways, for the layouts that switch on width.
     */
    protected fun snapshot(
        name: String,
        variants: List<ScreenshotVariant> = ScreenshotVariant.themes,
        isLandscape: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val directory = "$SCREENSHOT_DIRECTORY/${javaClass.simpleName}"
        variants.forEach { variant ->
            capture(
                filePath = "$directory/${name}_${variant.fileSuffix}$SCREENSHOT_EXTENSION",
                variant = variant,
                isLandscape = isLandscape,
                content = content,
            )
        }
    }

    /**
     * A capture owns the Compose rule that renders it: the rule holds one `setContent`, and it is
     * also what keeps the app's endless animations — the marquee, the shimmer, the now playing bars
     * — from spinning frames forever instead of settling into a frame to photograph.
     */
    private fun capture(
        filePath: String,
        variant: ScreenshotVariant,
        isLandscape: Boolean,
        content: @Composable () -> Unit,
    ) {
        applyConfiguration(variant = variant, isLandscape = isLandscape)
        val composeRule = createComposeRule()
        val statement = object : Statement() {
            override fun evaluate() {
                composeRule.setContent {
                    TuneScoutTheme(theme = variant.theme) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TuneScoutColors.background),
                        ) {
                            content()
                        }
                    }
                }
                composeRule.waitForIdle()
                composeRule.onRoot().captureRoboImage(filePath = filePath)
                composeRule.checkAccessibility()
            }
        }
        statement.runWithin(composeRule, filePath)
    }

    /** A dialog is a second root over the screen that opened it, and each root is checked. */
    private fun ComposeContentTestRule.checkAccessibility() {
        val roots = onAllNodes(isRoot())
        repeat(roots.fetchSemanticsNodes().size) { index ->
            roots[index].checkRoboAccessibility(accessibilityCheckOptions)
        }
    }

    private fun applyConfiguration(
        variant: ScreenshotVariant,
        isLandscape: Boolean,
    ) {
        val qualifiers = listOfNotNull(
            variant.locale,
            LANDSCAPE_QUALIFIER.takeIf { isLandscape },
        ).joinToString(separator = " ") { qualifier -> "+$qualifier" }
        if (qualifiers.isNotEmpty()) RuntimeEnvironment.setQualifiers(qualifiers)
        RuntimeEnvironment.setFontScale(variant.fontScale)
    }

    private companion object {
        const val LANDSCAPE_QUALIFIER = "w914dp-h411dp-land"
        const val SCREENSHOT_EXTENSION = ".png"

        /** Relative to the module, which is where a test runs: the images are versioned with it. */
        const val SCREENSHOT_DIRECTORY = "src/test/screenshots"
    }
}

/** Runs [this] inside [rule], which is what launches the activity the capture renders into. */
private fun Statement.runWithin(
    rule: org.junit.rules.TestRule,
    filePath: String,
) {
    rule.apply(this, Description.createSuiteDescription(filePath)).evaluate()
}
