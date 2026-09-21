package com.pierre.tunescout.screenshottests

import androidx.compose.runtime.Composable
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.presentation.content.AudioSearchContent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import org.junit.Test

internal class AudioSearchScreenshotTest : ScreenshotTest() {
    @Test
    fun waitingForSpeech() {
        snapshot(name = "waiting_for_speech", variants = ScreenshotVariant.all) {
            AudioSearchSheet(uiState = AudioSearchUiState.Listening(transcript = "", level = 0f))
        }
    }

    @Test
    fun hearing() {
        snapshot(name = "hearing") {
            AudioSearchSheet(uiState = AudioSearchUiState.Listening(transcript = "Daft Punk get lucky", level = 0.7f))
        }
    }

    @Test
    fun failed() {
        snapshot(name = "failed", variants = ScreenshotVariant.all) {
            AudioSearchSheet(uiState = AudioSearchUiState.Failed(SpeechFailure.NothingHeard))
        }
    }

    @Composable
    private fun AudioSearchSheet(uiState: AudioSearchUiState) {
        SheetOverScreen(
            screen = {},
            sheet = { AudioSearchContent(uiState = uiState, onEvent = {}) },
        )
    }
}
