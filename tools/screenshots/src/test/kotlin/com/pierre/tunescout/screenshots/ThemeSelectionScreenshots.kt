package com.pierre.tunescout.screenshots

import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.feature.themeselection.presentation.content.ThemeSelectionContent
import com.pierre.tunescout.feature.themeselection.presentation.mapper.toUiModel
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import com.pierre.tunescout.ui.theme.Theme
import org.junit.Test

internal class ThemeSelectionScreenshots : ReadmeScreenshotsTest() {
    private val themeSelection = ThemeSelectionUiState(
        options = Theme.entries.map { theme -> theme.toUiModel(selectedTheme = Theme.DARK) },
        isDynamicColorEnabled = false,
    )

    @Test
    fun theme() {
        capture(
            fileName = "theme",
            title = "Light, dark, or whatever the phone says",
            description = "The choice is remembered on the device, and can follow your wallpaper",
        ) {
            SheetOverScreen(
                screen = {
                    SongsContent(
                        isHeaderInline = false,
                        uiState = SongsUiState(
                            query = "",
                            recentlyPlayed = recentlyPlayed,
                            nowPlaying = null,
                            songPendingRemoval = null,
                            isOffline = false,
                            unplayableSongIds = emptySet(),
                        ),
                        searchResults = emptyPagingItems(),
                        onEvent = {},
                    )
                },
                sheet = { ThemeSelectionContent(uiState = themeSelection, onEvent = {}) },
            )
        }
    }
}
