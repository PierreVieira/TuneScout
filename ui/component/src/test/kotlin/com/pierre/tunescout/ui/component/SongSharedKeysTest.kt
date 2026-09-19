package com.pierre.tunescout.ui.component

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SongSharedKeysTest {
    @Test
    fun givenNoSongIdTheKeyIsNull() {
        // Given / When
        val key = getSongSharedKey(songId = null, element = SongSharedElement.ARTWORK)

        // Then
        assertThat(key).isNull()
    }

    @Test
    fun givenASongIdEachElementGetsItsOwnKey() {
        // Given / When
        val artwork = getSongSharedKey(songId = 7, element = SongSharedElement.ARTWORK)
        val title = getSongSharedKey(songId = 7, element = SongSharedElement.TITLE)

        // Then
        assertThat(artwork).isEqualTo(SongSharedKey(songId = 7, element = SongSharedElement.ARTWORK))
        assertThat(artwork).isNotEqualTo(title)
    }
}
