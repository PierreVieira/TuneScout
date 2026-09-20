package com.pierre.tunescout.core.database.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class SongEntityMapperTest {
    @Test
    fun `GIVEN a song WHEN mapping to entity and back THEN keeps every field`() {
        // Given
        val original = song(id = 7, duration = 215.seconds, trackNumber = 3)

        // When
        val roundTrip = original.toEntity().toSong()

        // Then
        assertThat(roundTrip).isEqualTo(original)
    }

    @Test
    fun `GIVEN an entity WHEN mapping to song THEN converts millis into a duration`() {
        // Given
        val entity = SongEntity(
            id = 1,
            title = "Get Lucky",
            artistName = "Daft Punk",
            albumId = 10,
            albumTitle = "Random Access Memories",
            artworkUrl = "https://example.com/art/100x100bb.jpg",
            previewUrl = "preview",
            durationMillis = 369_000,
            trackNumber = 8,
        )

        // When
        val song = entity.toSong()

        // Then
        assertThat(song.duration).isEqualTo(369.seconds)
    }
}
