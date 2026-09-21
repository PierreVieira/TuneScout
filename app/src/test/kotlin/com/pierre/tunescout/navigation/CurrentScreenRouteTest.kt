package com.pierre.tunescout.navigation

import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import org.junit.jupiter.api.Test

class CurrentScreenRouteTest {
    @Test
    fun `GIVEN a screen the player is reached from WHEN asking THEN the mini player is allowed`() {
        assertThat(listOf(HomeRoute).isMiniPlayerAllowed(isTwoPane = false)).isTrue()
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isMiniPlayerAllowed(isTwoPane = false)).isTrue()
    }

    @Test
    fun `GIVEN the player WHEN asking THEN the mini player is hidden`() {
        assertThat(listOf(HomeRoute, PlayerRoute(songId = 1)).isMiniPlayerAllowed(isTwoPane = false)).isFalse()
    }

    @Test
    fun `GIVEN a sheet over the player WHEN asking THEN the player still decides`() {
        assertThat(
            listOf(
                HomeRoute,
                PlayerRoute(songId = 1),
                SongOptionsRoute(songId = 1),
            ).isMiniPlayerAllowed(isTwoPane = false),
        ).isFalse()
        assertThat(
            listOf(HomeRoute, PlayerRoute(songId = 1), QueueRoute).isMiniPlayerAllowed(isTwoPane = false),
        ).isFalse()
    }

    @Test
    fun `GIVEN a sheet over a list WHEN asking THEN the list still decides`() {
        assertThat(listOf(HomeRoute, SongOptionsRoute(songId = 1)).isMiniPlayerAllowed(isTwoPane = false)).isTrue()
        assertThat(listOf(HomeRoute, QueueRoute).isMiniPlayerAllowed(isTwoPane = false)).isTrue()
    }

    @Test
    fun `GIVEN an empty back stack WHEN asking THEN the mini player is hidden`() {
        assertThat(emptyList<NavKey>().isMiniPlayerAllowed(isTwoPane = false)).isFalse()
    }

    @Test
    fun `GIVEN nothing but overlays WHEN asking THEN the mini player is hidden`() {
        assertThat(listOf<NavKey>(QueueRoute).isMiniPlayerAllowed(isTwoPane = false)).isFalse()
    }

    @Test
    fun `GIVEN two panes and the tabs on screen WHEN asking THEN the player pane replaces the mini player`() {
        assertThat(listOf(HomeRoute).isMiniPlayerAllowed(isTwoPane = true)).isFalse()
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isMiniPlayerAllowed(isTwoPane = true)).isFalse()
        assertThat(listOf(HomeRoute, SongOptionsRoute(songId = 1)).isMiniPlayerAllowed(isTwoPane = true)).isFalse()
    }

    @Test
    fun `GIVEN two panes and a screen covering the tabs WHEN asking THEN the mini player is allowed`() {
        assertThat(listOf(HomeRoute, FavoritesRoute).isMiniPlayerAllowed(isTwoPane = true)).isTrue()
    }

    @Test
    fun `GIVEN two panes and the player beside the tabs WHEN asking THEN the tab host is visible`() {
        assertThat(listOf(HomeRoute, PlayerRoute(songId = 1)).isHomeVisible(isTwoPane = true)).isTrue()
    }

    @Test
    fun `GIVEN the tab host WHEN asking THEN the navigation bar is visible`() {
        assertThat(listOf(HomeRoute).isHomeVisible(isTwoPane = false)).isTrue()
        assertThat(listOf(HomeRoute, SongOptionsRoute(songId = 1)).isHomeVisible(isTwoPane = false)).isTrue()
    }

    @Test
    fun `GIVEN a screen pushed over the tab host WHEN asking THEN the navigation bar is hidden`() {
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isHomeVisible(isTwoPane = false)).isFalse()
        assertThat(emptyList<NavKey>().isHomeVisible(isTwoPane = false)).isFalse()
    }

    @Test
    fun `GIVEN two panes and albums over the tab host WHEN asking THEN the navigation rail stays beside the tabs`() {
        assertThat(listOf(HomeRoute, AlbumRoute(albumId = 10)).isHomeVisible(isTwoPane = true)).isTrue()
        assertThat(
            listOf(HomeRoute, AlbumRoute(albumId = 10), AlbumRoute(albumId = 20)).isHomeVisible(isTwoPane = true),
        ).isTrue()
        assertThat(
            listOf(HomeRoute, AlbumRoute(albumId = 10), SongOptionsRoute(songId = 1)).isHomeVisible(isTwoPane = true),
        ).isTrue()
    }

    @Test
    fun `GIVEN two panes and a full screen over the album WHEN asking THEN the navigation rail is hidden`() {
        assertThat(
            listOf(HomeRoute, AlbumRoute(albumId = 10), FavoritesRoute).isHomeVisible(isTwoPane = true),
        ).isFalse()
        assertThat(
            listOf(HomeRoute, FavoritesRoute, AlbumRoute(albumId = 10)).isHomeVisible(isTwoPane = true),
        ).isFalse()
    }

    @Test
    fun `GIVEN two panes and the player over the album WHEN asking THEN it replaces the album beside the tabs`() {
        assertThat(
            listOf(HomeRoute, AlbumRoute(albumId = 10), PlayerRoute(songId = 1)).isHomeVisible(isTwoPane = true),
        ).isTrue()
    }
}
