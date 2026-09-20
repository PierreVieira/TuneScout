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
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.feature.player.presentation.content.PlayerContent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.feature.songoptions.presentation.content.SongOptionsContent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import com.pierre.tunescout.ui.theme.TuneScoutColors
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

private val sheetCornerRadius = 28.dp

internal class SongOptionsScreenshots : ReadmeScreenshotsTest() {
    private val playerBehindSheet = PlayerUiState.Loaded(
        song = getLucky,
        status = PlaybackStatus.Paused,
        position = 18.seconds,
        duration = 29.seconds,
        isRepeatEnabled = false,
        hasPrevious = true,
        hasNext = true,
    )

    @Test
    fun options() {
        capture(
            fileName = "options",
            title = "Queue it, or open its album",
            description = "The same sheet from every list and from the player",
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerContent(uiState = playerBehindSheet, isSideBySide = false, onEvent = {})
                ScrimBox()
                OptionsSheet()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrimBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BottomSheetDefaults.ScrimColor),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.OptionsSheet() {
    Surface(
        color = TuneScoutColors.sheet,
        shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BottomSheetDefaults.DragHandle()
            SongOptionsContent(
                uiState = SongOptionsUiState(
                    song = getLucky,
                    isRecentlyPlayed = true,
                    isFavorite = false,
                    isConfirmingRemoval = false,
                ),
                onEvent = {},
            )
        }
    }
}
