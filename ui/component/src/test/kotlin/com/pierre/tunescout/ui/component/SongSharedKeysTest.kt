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

    @Test
    fun givenAnotherSongIsPlayingTheRowKeepsTheKey() {
        // Given / When
        val sharedSongId = getRowSharedSongId(songId = 7, nowPlayingId = 8)

        // Then
        assertThat(sharedSongId).isEqualTo(7)
    }

    @Test
    fun givenNothingIsPlayingTheRowKeepsTheKey() {
        // Given / When
        val sharedSongId = getRowSharedSongId(songId = 7, nowPlayingId = null)

        // Then
        assertThat(sharedSongId).isEqualTo(7)
    }

    @Test
    fun givenTheRowIsTheSongPlayingItGivesTheKeyToTheMiniPlayer() {
        // Given / When
        val sharedSongId = getRowSharedSongId(songId = 7, nowPlayingId = 7)

        // Then
        assertThat(sharedSongId).isNull()
    }
}
