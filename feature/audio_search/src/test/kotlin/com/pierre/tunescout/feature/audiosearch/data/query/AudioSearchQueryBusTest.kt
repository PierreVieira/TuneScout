package com.pierre.tunescout.feature.audiosearch.data.query

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AudioSearchQueryBusTest {
    private lateinit var bus: AudioSearchQueryBus

    @BeforeEach
    fun setUp() {
        bus = AudioSearchQueryBus()
    }

    @Test
    fun `WHEN a query is published THEN whoever is observing receives it`() = runTest {
        bus.observeQueries().test {
            // When
            bus.publish("daft punk")

            // Then
            assertThat(awaitItem()).isEqualTo("daft punk")
        }
    }

    @Test
    fun `WHEN observing after a query was published THEN it is not replayed`() = runTest {
        // When
        bus.publish("daft punk")

        // Then
        bus.observeQueries().test {
            expectNoEvents()
        }
    }
}
