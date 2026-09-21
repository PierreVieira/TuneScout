package com.pierre.tunescout.screenshottests

import androidx.compose.runtime.Composable
import com.pierre.tunescout.feature.themeselection.presentation.content.DynamicColorInfoContent
import com.pierre.tunescout.feature.themeselection.presentation.content.ThemeSelectionContent
import com.pierre.tunescout.feature.themeselection.presentation.mapper.toUiModel
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.ui.theme.Theme
import org.junit.Test

internal class ThemeSelectionScreenshotTest : ScreenshotTest() {
    @Test
    fun themes() {
        snapshot(name = "themes", variants = ScreenshotVariant.all) {
            ThemeSheet(selectedTheme = Theme.SYSTEM, isDynamicColorEnabled = false)
        }
    }

    @Test
    fun dynamicColorOn() {
        snapshot(name = "dynamic_color_on") {
            ThemeSheet(selectedTheme = Theme.DARK, isDynamicColorEnabled = true)
        }
    }

    /** Dynamic color is unsupported below API 31, and the toggle is hidden rather than dead. */
    @Test
    fun withoutDynamicColor() {
        snapshot(name = "without_dynamic_color") {
            ThemeSheet(selectedTheme = Theme.LIGHT, isDynamicColorEnabled = null)
        }
    }

    @Test
    fun dynamicColorInfo() {
        snapshot(name = "dynamic_color_info") {
            DynamicColorInfoContent(uiState = true, onEvent = {})
        }
    }

    @Composable
    private fun ThemeSheet(
        selectedTheme: Theme,
        isDynamicColorEnabled: Boolean?,
    ) {
        SheetOverScreen(
            screen = {},
            sheet = {
                ThemeSelectionContent(
                    uiState = ThemeSelectionUiState(
                        options = Theme.entries.map { theme -> theme.toUiModel(selectedTheme = selectedTheme) },
                        isDynamicColorEnabled = isDynamicColorEnabled,
                    ),
                    onEvent = {},
                )
            },
        )
    }
}
