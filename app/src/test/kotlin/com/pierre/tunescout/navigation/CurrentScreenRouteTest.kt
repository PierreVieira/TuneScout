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
        assertThat(listOf(HomeRoute).isMiniPlayerAllowed()).isTrue()
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isMiniPlayerAllowed()).isTrue()
    }

    @Test
    fun `GIVEN the player WHEN asking THEN the mini player is hidden`() {
        assertThat(listOf(HomeRoute, PlayerRoute(songId = 1)).isMiniPlayerAllowed()).isFalse()
    }

    @Test
    fun `GIVEN a sheet over the player WHEN asking THEN the player still decides`() {
        assertThat(
            listOf(HomeRoute, PlayerRoute(songId = 1), SongOptionsRoute(songId = 1)).isMiniPlayerAllowed(),
        ).isFalse()
        assertThat(
            listOf(HomeRoute, PlayerRoute(songId = 1), QueueRoute).isMiniPlayerAllowed(),
        ).isFalse()
    }

    @Test
    fun `GIVEN a sheet over a list WHEN asking THEN the list still decides`() {
        assertThat(listOf(HomeRoute, SongOptionsRoute(songId = 1)).isMiniPlayerAllowed()).isTrue()
        assertThat(listOf(HomeRoute, QueueRoute).isMiniPlayerAllowed()).isTrue()
    }

    @Test
    fun `GIVEN the splash or an empty back stack WHEN asking THEN the mini player is hidden`() {
        assertThat(listOf(SplashRoute).isMiniPlayerAllowed()).isFalse()
        assertThat(emptyList<NavKey>().isMiniPlayerAllowed()).isFalse()
    }

    @Test
    fun `GIVEN nothing but overlays WHEN asking THEN the mini player is hidden`() {
        assertThat(listOf<NavKey>(QueueRoute).isMiniPlayerAllowed()).isFalse()
    }

    @Test
    fun `GIVEN the tab host WHEN asking THEN the navigation bar is visible`() {
        assertThat(listOf(HomeRoute).isHomeVisible()).isTrue()
        assertThat(listOf(HomeRoute, SongOptionsRoute(songId = 1)).isHomeVisible()).isTrue()
    }

    @Test
    fun `GIVEN a screen pushed over the tab host WHEN asking THEN the navigation bar is hidden`() {
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isHomeVisible()).isFalse()
        assertThat(listOf(SplashRoute).isHomeVisible()).isFalse()
        assertThat(emptyList<NavKey>().isHomeVisible()).isFalse()
    }
}
