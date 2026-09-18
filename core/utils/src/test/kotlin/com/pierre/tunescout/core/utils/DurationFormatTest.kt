package com.pierre.tunescout.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.time.Duration.Companion.milliseconds

class DurationFormatTest {
    @ParameterizedTest(name = "GIVEN {0}ms WHEN formatting THEN shows {1}")
    @CsvSource(
        "0, 0:00",
        "999, 0:00",
        "5000, 0:05",
        "86000, 1:26",
        "174000, 2:54",
        "3600000, 60:00",
        "-5000, 0:00",
    )
    fun `formats as minutes and zero padded seconds`(
        millis: Long,
        expected: String,
    ) {
        // When
        val formatted = millis.milliseconds.toClockString()

        // Then
        assertThat(formatted).isEqualTo(expected)
    }
}
