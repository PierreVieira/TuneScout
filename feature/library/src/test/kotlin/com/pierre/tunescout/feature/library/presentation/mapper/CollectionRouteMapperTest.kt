package com.pierre.tunescout.feature.library.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.DownloadedSongsOptionsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesOptionsRoute
import com.pierre.tunescout.core.navigation.route.PlaylistOptionsRoute
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import org.junit.jupiter.api.Test

class CollectionRouteMapperTest {
    @Test
    fun `GIVEN the favourites WHEN mapping to their options route THEN it needs no id`() {
        // When
        val route = CollectionKey.Favorites.toOptionsRoute()

        // Then
        assertThat(route).isEqualTo(FavoritesOptionsRoute)
    }

    @Test
    fun `GIVEN a playlist WHEN mapping to its options route THEN it carries the playlist id`() {
        // When
        val route = CollectionKey.Playlist(playlistId = 7).toOptionsRoute()

        // Then
        assertThat(route).isEqualTo(PlaylistOptionsRoute(playlistId = 7))
    }

    @Test
    fun `GIVEN the songs downloaded on their own WHEN mapping to their options route THEN it needs no id`() {
        // When
        val route = CollectionKey.DownloadedSongs.toOptionsRoute()

        // Then
        assertThat(route).isEqualTo(DownloadedSongsOptionsRoute)
    }
}
