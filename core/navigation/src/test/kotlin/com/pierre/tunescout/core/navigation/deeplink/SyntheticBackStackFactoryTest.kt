package com.pierre.tunescout.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyntheticBackStackFactoryTest {
    private lateinit var factory: SyntheticBackStackFactory

    @BeforeEach
    fun setUp() {
        factory = SyntheticBackStackFactory()
    }

    @Test
    fun `GIVEN a deep link route WHEN building the back stack THEN puts its parent under it`() {
        // Given
        val route = PlayerRoute(songId = 7)

        // When
        val backStack = factory.buildBackStack(route)

        // Then
        assertThat(backStack).containsExactly(HomeRoute, route).inOrder()
    }

    @Test
    fun `GIVEN a chain of deep link routes WHEN building the back stack THEN walks up to the root`() {
        // Given
        val route = GrandChildRoute

        // When
        val backStack = factory.buildBackStack(route)

        // Then
        assertThat(backStack).containsExactly(HomeRoute, ChildRoute, GrandChildRoute).inOrder()
    }

    @Test
    fun `GIVEN a route that is nobody's child WHEN building the back stack THEN returns it alone`() {
        // When
        val backStack = factory.buildBackStack(HomeRoute)

        // Then
        assertThat(backStack).containsExactly(HomeRoute)
    }
}

private data object ChildRoute : DeepLinkKey {
    override val parent: NavKey = HomeRoute
}

private data object GrandChildRoute : DeepLinkKey {
    override val parent: NavKey = ChildRoute
}
