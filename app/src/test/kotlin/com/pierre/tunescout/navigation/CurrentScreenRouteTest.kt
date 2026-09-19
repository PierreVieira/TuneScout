package com.pierre.tunescout.navigation

import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.route.SplashRoute
import org.junit.jupiter.api.Test

class CurrentScreenRouteTest {
    @Test
    fun `GIVEN a screen the player is reached from WHEN asking THEN the mini player is allowed`() {
        assertThat(isMiniPlayerAllowed(listOf(HomeRoute))).isTrue()
        assertThat(isMiniPlayerAllowed(listOf(HomeRoute, AlbumRoute(albumId = 10)))).isTrue()
    }

    @Test
    fun `GIVEN the player WHEN asking THEN the mini player is hidden`() {
        assertThat(isMiniPlayerAllowed(listOf(HomeRoute, PlayerRoute(songId = 1)))).isFalse()
    }

    @Test
    fun `GIVEN a sheet over the player WHEN asking THEN the player still decides`() {
        assertThat(
            isMiniPlayerAllowed(
                listOf(HomeRoute, PlayerRoute(songId = 1), SongOptionsRoute(songId = 1)),
            ),
        ).isFalse()
        assertThat(
            isMiniPlayerAllowed(listOf(HomeRoute, PlayerRoute(songId = 1), QueueRoute)),
        ).isFalse()
    }

    @Test
    fun `GIVEN a sheet over a list WHEN asking THEN the list still decides`() {
        assertThat(isMiniPlayerAllowed(listOf(HomeRoute, SongOptionsRoute(songId = 1)))).isTrue()
        assertThat(isMiniPlayerAllowed(listOf(HomeRoute, QueueRoute))).isTrue()
    }

    @Test
    fun `GIVEN the splash or an empty back stack WHEN asking THEN the mini player is hidden`() {
        assertThat(isMiniPlayerAllowed(listOf(SplashRoute))).isFalse()
        assertThat(isMiniPlayerAllowed(emptyList())).isFalse()
    }

    @Test
    fun `GIVEN nothing but overlays WHEN asking THEN the mini player is hidden`() {
        assertThat(isMiniPlayerAllowed(listOf<NavKey>(QueueRoute))).isFalse()
    }

    @Test
    fun `GIVEN the tab host WHEN asking THEN the navigation bar is visible`() {
        assertThat(isHomeVisible(listOf(HomeRoute))).isTrue()
        assertThat(isHomeVisible(listOf(HomeRoute, SongOptionsRoute(songId = 1)))).isTrue()
    }

    @Test
    fun `GIVEN a screen pushed over the tab host WHEN asking THEN the navigation bar is hidden`() {
        assertThat(isHomeVisible(listOf(HomeRoute, AlbumRoute(albumId = 10)))).isFalse()
        assertThat(isHomeVisible(listOf(SplashRoute))).isFalse()
        assertThat(isHomeVisible(emptyList())).isFalse()
    }
}
