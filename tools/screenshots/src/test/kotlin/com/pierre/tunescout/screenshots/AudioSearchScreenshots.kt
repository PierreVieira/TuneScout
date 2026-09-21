package com.pierre.tunescout.screenshots

import com.pierre.tunescout.feature.audiosearch.presentation.content.AudioSearchContent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiState
import com.pierre.tunescout.feature.songs.presentation.content.SongsContent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.screenshotfixtures.SheetOverScreen
import com.pierre.tunescout.screenshotfixtures.emptyPagingItems
import com.pierre.tunescout.screenshotfixtures.recentlyPlayed
import org.junit.Test

internal class AudioSearchScreenshots : ReadmeScreenshotsTest() {
    /** Mid-sentence and loud, so the halos are drawn wide open instead of at rest. */
    private val hearing = AudioSearchUiState.Listening(transcript = "Daft Punk get lucky", level = 0.8f)

    @Test
    fun audioSearch() {
        capture(
            fileName = "audio_search",
            title = "Or just say it",
            description = "The words appear as you speak, and the search starts when you stop",
        ) {
            SheetOverScreen(
                screen = {
                    SongsContent(
                        isHeaderInline = false,
                        uiState = SongsUiState(
                            query = "",
                            isAudioSearchAvailable = true,
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
                sheet = { AudioSearchContent(uiState = hearing, onEvent = {}) },
            )
        }
    }
}
