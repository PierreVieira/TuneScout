package com.pierre.tunescout.core.utils

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SuspendRunCatchingTest {
    @Test
    fun `GIVEN a block that succeeds WHEN running THEN returns success with its value`() = runTest {
        // Given
        val block = { "songs" }

        // When
        val result = suspendRunCatching(block)

        // Then
        assertThat(result.getOrNull()).isEqualTo("songs")
    }

    @Test
    fun `GIVEN a block that throws WHEN running THEN returns failure with the exception`() = runTest {
        // Given
        val exception = IllegalStateException("boom")
        val block = { throw exception }

        // When
        val result = suspendRunCatching(block)

        // Then
        assertThat(result.exceptionOrNull()).isSameInstanceAs(exception)
    }

    @Test
    fun `GIVEN a cancelled coroutine WHEN running THEN rethrows the cancellation`() = runTest {
        // Given
        val block = { throw CancellationException("cancelled") }

        // When / Then
        assertThrows<CancellationException> { suspendRunCatching(block) }
    }
}
