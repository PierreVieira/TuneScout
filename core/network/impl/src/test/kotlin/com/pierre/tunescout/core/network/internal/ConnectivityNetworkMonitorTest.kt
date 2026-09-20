package com.pierre.tunescout.core.network.internal

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ConnectivityNetworkMonitorTest {
    private lateinit var monitor: ConnectivityNetworkMonitor
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var callback: CapturingSlot<ConnectivityManager.NetworkCallback>

    @Test
    fun `GIVEN a validated network WHEN observing THEN emits online before any callback`() = runTest {
        // Given
        prepareScenario(hasValidatedNetwork = true)

        // When
        monitor.observeIsOnline().test {
            // Then
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN no active network WHEN observing THEN emits offline before any callback`() = runTest {
        // Given
        prepareScenario(hasActiveNetwork = false)

        // When
        monitor.observeIsOnline().test {
            // Then
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN a network that never validated WHEN observing THEN emits offline`() = runTest {
        // Given
        prepareScenario(hasValidatedNetwork = false)

        // When
        monitor.observeIsOnline().test {
            // Then
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an offline device WHEN a network becomes available THEN emits online`() = runTest {
        // Given
        prepareScenario(hasActiveNetwork = false)

        // When
        monitor.observeIsOnline().test {
            awaitItem()
            callback.captured.onAvailable(mockk<Network>())

            // Then
            assertThat(awaitItem()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an online device WHEN the network is lost THEN emits offline`() = runTest {
        // Given
        prepareScenario(hasValidatedNetwork = true)

        // When
        monitor.observeIsOnline().test {
            awaitItem()
            callback.captured.onLost(mockk<Network>())

            // Then
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an offline device WHEN no network can be found THEN emits offline once`() = runTest {
        // Given
        prepareScenario(hasActiveNetwork = false)

        // When
        monitor.observeIsOnline().test {
            assertThat(awaitItem()).isFalse()
            callback.captured.onUnavailable()

            // Then
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an online device WHEN another network becomes available THEN does not emit again`() = runTest {
        // Given
        prepareScenario(hasValidatedNetwork = true)

        // When
        monitor.observeIsOnline().test {
            assertThat(awaitItem()).isTrue()
            callback.captured.onAvailable(mockk<Network>())

            // Then
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GIVEN an observed monitor WHEN the collector stops THEN unregisters the callback`() = runTest {
        // Given
        prepareScenario(hasValidatedNetwork = true)

        // When
        monitor.observeIsOnline().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Then
        verify { connectivityManager.unregisterNetworkCallback(callback.captured) }
    }

    private fun prepareScenario(
        hasActiveNetwork: Boolean = true,
        hasValidatedNetwork: Boolean = false,
    ) {
        val capabilities = mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns hasValidatedNetwork
        }
        val activeNetwork = mockk<Network>()
        callback = slot()
        connectivityManager = mockk {
            every { this@mockk.activeNetwork } returns activeNetwork.takeIf { hasActiveNetwork }
            every { getNetworkCapabilities(activeNetwork) } returns capabilities
            every { registerDefaultNetworkCallback(capture(callback)) } just runs
            every { unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
        }
        monitor = ConnectivityNetworkMonitor(connectivityManager = connectivityManager)
    }
}
