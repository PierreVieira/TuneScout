package com.pierre.tunescout.navigation

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.route.SongsRoute
import com.pierre.tunescout.core.navigation.route.SplashRoute
import org.junit.jupiter.api.Test

class MiniPlayerRoutesTest {
    @Test
    fun `GIVEN a screen the player is reached from WHEN asking THEN the mini player is allowed`() {
        assertThat(isMiniPlayerAllowed(SongsRoute)).isTrue()
        assertThat(isMiniPlayerAllowed(AlbumRoute(albumId = 10))).isTrue()
        assertThat(isMiniPlayerAllowed(SongOptionsRoute(songId = 1))).isTrue()
    }

    @Test
    fun `GIVEN the player or the queue it opens WHEN asking THEN the mini player is hidden`() {
        assertThat(isMiniPlayerAllowed(PlayerRoute(songId = 1))).isFalse()
        assertThat(isMiniPlayerAllowed(QueueRoute)).isFalse()
    }

    @Test
    fun `GIVEN the splash or an empty back stack WHEN asking THEN the mini player is hidden`() {
        assertThat(isMiniPlayerAllowed(SplashRoute)).isFalse()
        assertThat(isMiniPlayerAllowed(null)).isFalse()
    }
}
