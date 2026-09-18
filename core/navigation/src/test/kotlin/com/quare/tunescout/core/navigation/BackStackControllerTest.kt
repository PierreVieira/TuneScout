package com.quare.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.quare.tunescout.core.navigation.route.AlbumRoute
import com.quare.tunescout.core.navigation.route.PlayerRoute
import com.quare.tunescout.core.navigation.route.SongsRoute
import org.junit.jupiter.api.Test

class BackStackControllerTest {
    private val songsRoute: NavKey = SongsRoute
    private val playerRoute: NavKey = PlayerRoute(songId = 1)
    private val albumRoute: NavKey = AlbumRoute(albumId = 10)

    private lateinit var backStackController: BackStackController
    private lateinit var backStack: MutableList<NavKey>

    @Test
    fun `GIVEN a route different from the top WHEN navigating THEN pushes it on top`() {
        // Given
        prepareScenario()

        // When
        backStackController.navigate(playerRoute)

        // Then
        assertThat(backStack).containsExactly(songsRoute, playerRoute).inOrder()
    }

    @Test
    fun `GIVEN the route already on top WHEN navigating THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(songsRoute, playerRoute))

        // When
        backStackController.navigate(playerRoute)

        // Then
        assertThat(backStack).containsExactly(songsRoute, playerRoute).inOrder()
    }

    @Test
    fun `GIVEN a route deeper in the stack WHEN navigating THEN pushes it again on top`() {
        // Given
        prepareScenario(backStack = listOf(songsRoute, playerRoute, albumRoute))

        // When
        backStackController.navigate(playerRoute)

        // Then
        assertThat(backStack).containsExactly(songsRoute, playerRoute, albumRoute, playerRoute).inOrder()
    }

    @Test
    fun `GIVEN a route different from the top WHEN navigating replacing top THEN swaps the top entry`() {
        // Given
        prepareScenario(backStack = listOf(songsRoute, playerRoute))

        // When
        backStackController.navigateReplacingTop(albumRoute)

        // Then
        assertThat(backStack).containsExactly(songsRoute, albumRoute).inOrder()
    }

    @Test
    fun `GIVEN the route already on top WHEN navigating replacing top THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(songsRoute, playerRoute))

        // When
        backStackController.navigateReplacingTop(playerRoute)

        // Then
        assertThat(backStack).containsExactly(songsRoute, playerRoute).inOrder()
    }

    @Test
    fun `GIVEN a stacked route WHEN navigating back THEN pops it`() {
        // Given
        prepareScenario(backStack = listOf(songsRoute, playerRoute))

        // When
        backStackController.navigateBack()

        // Then
        assertThat(backStack).containsExactly(songsRoute)
    }

    @Test
    fun `GIVEN only the root entry WHEN navigating back THEN keeps the root`() {
        // Given
        prepareScenario()

        // When
        backStackController.navigateBack()

        // Then
        assertThat(backStack).containsExactly(songsRoute)
    }

    private fun prepareScenario(backStack: List<NavKey> = listOf(songsRoute)) {
        this.backStack = backStack.toMutableList()
        backStackController = BackStackController(backStack = this.backStack)
    }
}
