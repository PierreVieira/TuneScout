package com.pierre.tunescout.feature.splash.presentation.viewmodel

import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModelTest {
    private lateinit var navigator: Navigator

    @BeforeEach
    fun setUp() {
        navigator = mockk(relaxUnitFun = true)
    }

    @Test
    fun `WHEN the hold elapses THEN replaces the splash with the songs screen`() = runTest(mainDispatcher.dispatcher) {
        SplashViewModel(navigator = navigator, holdDuration = 500.milliseconds)

        advanceTimeBy(501.milliseconds)
        runCurrent()

        verify { navigator.navigateReplacingTop(HomeRoute) }
    }

    @Test
    fun `WHEN the hold has not elapsed THEN stays on the splash`() = runTest(mainDispatcher.dispatcher) {
        SplashViewModel(navigator = navigator, holdDuration = 500.milliseconds)

        advanceTimeBy(100.milliseconds)
        runCurrent()

        verify(exactly = 0) { navigator.navigateReplacingTop(any()) }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
