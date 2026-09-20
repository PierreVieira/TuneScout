package com.pierre.tunescout.ui.component

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SongSharedKeysTest {
    @Test
    fun givenNoSongIdTheKeyIsNull() {
        // Given / When
        val key = SongSharedKey.createOrNull(songId = null, element = SongSharedElement.ARTWORK)

        // Then
        assertThat(key).isNull()
    }

    @Test
    fun givenASongIdEachElementGetsItsOwnKey() {
        // Given / When
        val artwork = SongSharedKey.createOrNull(songId = 7, element = SongSharedElement.ARTWORK)
        val title = SongSharedKey.createOrNull(songId = 7, element = SongSharedElement.TITLE)

        // Then
        assertThat(artwork).isEqualTo(SongSharedKey(songId = 7, element = SongSharedElement.ARTWORK))
        assertThat(artwork).isNotEqualTo(title)
    }
}
