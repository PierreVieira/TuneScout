package com.pierre.tunescout.core.navigation.reorder

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SharedFlowReorderRequestsTest {
    private val requests = SharedFlowReorderRequests()

    @Test
    fun `GIVEN a screen observing an album WHEN reordering it is requested THEN the screen hears it`() = runTest {
        // Given
        val target = ReorderTarget.Album(albumId = 10)

        // When
        requests.observe(target).test {
            requests.request(target)

            // Then
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `GIVEN a screen observing an album WHEN another list is requested THEN the screen hears nothing`() = runTest {
        // Given
        val target = ReorderTarget.Album(albumId = 10)

        // When
        requests.observe(target).test {
            requests.request(ReorderTarget.Playlist(playlistId = 10))
            requests.request(ReorderTarget.Album(albumId = 11))

            // Then
            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN a request nobody heard WHEN a screen starts observing THEN it is not replayed`() = runTest {
        // Given
        val target = ReorderTarget.Playlist(playlistId = 3)
        requests.request(target)
        var heard = false

        // When
        backgroundScope.launch { requests.observe(target).first().also { heard = true } }
        runCurrent()

        // Then
        assertThat(heard).isFalse()
    }
}
