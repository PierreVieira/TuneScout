package com.pierre.tunescout.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UuidIdGeneratorTest {
    @Test
    fun `WHEN creating an id THEN it is a parseable uuid`() {
        // Given
        val idGenerator = UuidIdGenerator()

        // When
        val id = idGenerator.createId()

        // Then
        assertThat(UUID.fromString(id).toString()).isEqualTo(id)
    }

    @Test
    fun `WHEN creating ids in a row THEN each one is different`() {
        // Given
        val idGenerator = UuidIdGenerator()

        // When
        val ids = List(100) { idGenerator.createId() }

        // Then
        assertThat(ids).containsNoDuplicates()
    }
}
