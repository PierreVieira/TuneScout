package com.pierre.tunescout.core.network.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.network.dto.ResultDto
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class SongMapperTest {
    @Test
    fun `GIVEN a complete song result WHEN mapping THEN builds the song`() {
        // Given
        val dto = songResult()

        // When
        val song = dto.toSongOrNull()

        // Then
        assertThat(song).isNotNull()
        assertThat(song?.id).isEqualTo(1L)
        assertThat(song?.title).isEqualTo("Get Lucky")
        assertThat(song?.artistName).isEqualTo("Daft Punk")
        assertThat(song?.albumId).isEqualTo(10L)
        assertThat(song?.albumTitle).isEqualTo("Random Access Memories")
        assertThat(song?.artwork).isEqualTo(Artwork("https://example.com/art/100x100bb.jpg"))
        assertThat(song?.duration).isEqualTo(369_000.milliseconds)
        assertThat(song?.trackNumber).isEqualTo(8)
    }

    @Test
    fun `GIVEN a result that is not a song WHEN mapping THEN returns null`() {
        // Given
        val dto = songResult().copy(wrapperType = "collection", kind = null)

        // When
        val song = dto.toSongOrNull()

        // Then
        assertThat(song).isNull()
    }

    @Test
    fun `GIVEN a song without a preview WHEN mapping THEN returns null`() {
        // Given
        val dto = songResult().copy(previewUrl = null)

        // When
        val song = dto.toSongOrNull()

        // Then
        assertThat(song).isNull()
    }

    @Test
    fun `GIVEN a song without artwork or duration WHEN mapping THEN falls back to empty values`() {
        // Given
        val dto = songResult().copy(artworkUrl100 = null, trackTimeMillis = null, trackNumber = null)

        // When
        val song = dto.toSongOrNull()

        // Then
        assertThat(song?.artwork?.sourceUrl).isEmpty()
        assertThat(song?.duration).isEqualTo(0.milliseconds)
        assertThat(song?.trackNumber).isEqualTo(0)
    }
}

internal fun songResult(): ResultDto = ResultDto(
    wrapperType = "track",
    kind = "song",
    trackId = 1L,
    trackName = "Get Lucky",
    artistName = "Daft Punk",
    collectionId = 10L,
    collectionName = "Random Access Memories",
    artworkUrl100 = "https://example.com/art/100x100bb.jpg",
    previewUrl = "https://example.com/preview.m4a",
    trackTimeMillis = 369_000L,
    trackNumber = 8,
)
