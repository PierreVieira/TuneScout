package com.pierre.tunescout.core.navigation.deeplink

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TuneScoutDeepLinkMatcherTest {
    private lateinit var matcher: TuneScoutDeepLinkMatcher

    @BeforeEach
    fun setUp() {
        matcher = TuneScoutDeepLinkMatcher()
    }

    @Test
    fun `GIVEN a player url WHEN matching THEN returns the player route of that song`() {
        // Given
        val url = DeepLinkUrls.createPlayerUrl(songId = 42)

        // When
        val route = matcher.findRouteOrNull(url)

        // Then
        assertThat(route).isEqualTo(PlayerRoute(songId = 42))
    }

    @Test
    fun `GIVEN a player url without a song WHEN matching THEN returns no route`() {
        // Given
        val url = "${DeepLinkUrls.SCHEME}://${DeepLinkUrls.PLAYER_HOST}"

        // When
        val route = matcher.findRouteOrNull(url)

        // Then
        assertThat(route).isNull()
    }

    @Test
    fun `GIVEN a player url whose song is not a number WHEN matching THEN returns no route`() {
        // Given
        val url = "${DeepLinkUrls.SCHEME}://${DeepLinkUrls.PLAYER_HOST}/latest"

        // When
        val route = matcher.findRouteOrNull(url)

        // Then
        assertThat(route).isNull()
    }

    @Test
    fun `GIVEN a url of another scheme WHEN matching THEN returns no route`() {
        // Given
        val url = "https://tunescout.com/player/42"

        // When
        val route = matcher.findRouteOrNull(url)

        // Then
        assertThat(route).isNull()
    }

    @Test
    fun `GIVEN a url the app does not know WHEN matching THEN returns no route`() {
        // Given
        val url = "${DeepLinkUrls.SCHEME}://queue"

        // When
        val route = matcher.findRouteOrNull(url)

        // Then
        assertThat(route).isNull()
    }

    @Test
    fun `GIVEN an intent without data WHEN matching THEN returns no route`() {
        // When
        val route = matcher.findRouteOrNull(null)

        // Then
        assertThat(route).isNull()
    }
}
