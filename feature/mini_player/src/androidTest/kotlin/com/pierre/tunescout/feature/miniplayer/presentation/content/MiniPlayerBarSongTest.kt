package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class MiniPlayerBarSongTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    @Test
    fun whileTheBarIsVisibleItFollowsTheSongPlaying() = compose.use {
        // Given
        val playing = mutableStateOf(song(id = 1, title = "Get Lucky"))
        var barSong: Song? = null
        setContent {
            barSong = rememberBarSong(song = playing.value, isVisible = true)
        }

        // When
        playing.value = song(id = 2, title = "Instant Crush")
        waitForIdle()

        // Then
        assertThat(barSong?.id).isEqualTo(2)
    }

    @Test
    fun onceTheBarIsLeavingItHoldsTheSongItWasShowing() = compose.use {
        // Given
        val playing = mutableStateOf<Song?>(song(id = 1, title = "Get Lucky"))
        val isVisible = mutableStateOf(true)
        var barSong: Song? = null
        setContent {
            barSong = rememberBarSong(song = playing.value, isVisible = isVisible.value)
        }

        // When
        isVisible.value = false
        playing.value = song(id = 2, title = "Instant Crush")
        waitForIdle()

        // Then
        assertThat(barSong?.id).isEqualTo(1)
    }
}
