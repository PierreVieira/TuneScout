package com.pierre.tunescout.feature.library.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class LibraryGridColumnsTest {
    @Test
    fun `GIVEN two per row WHEN asking for the next size THEN it is three`() {
        // When
        val next = LibraryGridColumns.TWO.next

        // Then
        assertThat(next).isEqualTo(LibraryGridColumns.THREE)
    }

    @Test
    fun `GIVEN three per row WHEN asking for the next size THEN it is four`() {
        // When
        val next = LibraryGridColumns.THREE.next

        // Then
        assertThat(next).isEqualTo(LibraryGridColumns.FOUR)
    }

    @Test
    fun `GIVEN four per row WHEN asking for the next size THEN it is two again`() {
        // When
        val next = LibraryGridColumns.FOUR.next

        // Then
        assertThat(next).isEqualTo(LibraryGridColumns.TWO)
    }
}
