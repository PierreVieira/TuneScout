package com.pierre.tunescout.core.navigation.deeplink

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DeepLinkUrlsTest {
    @Test
    fun `GIVEN a song WHEN creating the player url THEN names the app scheme, the player and the song`() {
        // When
        val url = DeepLinkUrls.createPlayerUrl(songId = 99)

        // Then
        assertThat(url).isEqualTo("tunescout://player/99")
    }
}
