package com.pierre.tunescout.core.navigation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChannelNavigatorTest {
    private lateinit var navigator: ChannelNavigator

    @BeforeEach
    fun setUp() {
        navigator = ChannelNavigator()
    }

    @Test
    fun `GIVEN a route WHEN navigating THEN emits a Navigate command with it`() = runTest {
        navigator.commands.test {
            // When
            navigator.navigate(PlayerRoute(songId = 42))

            // Then
            assertThat(awaitItem()).isEqualTo(NavigationCommand.Navigate(PlayerRoute(songId = 42)))
        }
    }

    @Test
    fun `GIVEN a route WHEN replacing the top THEN emits a ReplaceTop command with it`() = runTest {
        navigator.commands.test {
            // When
            navigator.navigateReplacingTop(AlbumRoute(albumId = 7))

            // Then
            assertThat(awaitItem()).isEqualTo(NavigationCommand.ReplaceTop(AlbumRoute(albumId = 7)))
        }
    }

    @Test
    fun `GIVEN a back stack WHEN resetting to it THEN emits a ResetTo command with its routes`() = runTest {
        // Given
        val routes = listOf(AlbumRoute(albumId = 7), PlayerRoute(songId = 42))

        navigator.commands.test {
            // When
            navigator.navigateResettingTo(routes)

            // Then
            assertThat(awaitItem()).isEqualTo(NavigationCommand.ResetTo(routes))
        }
    }

    @Test
    fun `WHEN navigating back THEN emits a Back command`() = runTest {
        navigator.commands.test {
            // When
            navigator.navigateBack()

            // Then
            assertThat(awaitItem()).isEqualTo(NavigationCommand.Back)
        }
    }

    @Test
    fun `GIVEN commands sent before collection WHEN collecting THEN delivers them in order`() = runTest {
        // Given
        navigator.navigate(PlayerRoute(songId = 1))
        navigator.navigateBack()

        // When / Then
        navigator.commands.test {
            assertThat(awaitItem()).isEqualTo(NavigationCommand.Navigate(PlayerRoute(songId = 1)))
            assertThat(awaitItem()).isEqualTo(NavigationCommand.Back)
        }
    }
}
