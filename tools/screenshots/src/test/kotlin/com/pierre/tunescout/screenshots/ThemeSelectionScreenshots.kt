package com.pierre.tunescout.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.feature.themeselection.presentation.content.ThemeSelectionContent
import com.pierre.tunescout.feature.themeselection.presentation.mapper.toUiModel
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutColors
import org.junit.Test

internal class ThemeSelectionScreenshots : ReadmeScreenshotsTest() {
    private val sheetCornerRadius = 28.dp
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
            Box(modifier = Modifier.fillMaxSize()) {
                SongsContent(
                    uiState = SongsUiState(query = "", recentlyPlayed = recentlyPlayed, nowPlayingId = null),
                    searchResults = emptyPagingItems(),
                    onEvent = {},
                )
                Scrim()
                ThemeSheet()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Scrim() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BottomSheetDefaults.ScrimColor),
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BoxScope.ThemeSheet() {
        Surface(
            color = TuneScoutColors.sheet,
            shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomSheetDefaults.DragHandle()
                ThemeSelectionContent(uiState = themeSelection, onEvent = {})
            }
        }
    }
}
