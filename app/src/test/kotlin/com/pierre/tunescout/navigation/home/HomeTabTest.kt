package com.pierre.tunescout.navigation.home

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.route.LibraryRoute
import com.pierre.tunescout.core.navigation.route.SongsRoute
import org.junit.jupiter.api.Test

class HomeTabTest {
    @Test
    fun `WHEN reading the tabs THEN search comes before the library`() {
        // When
        val tabs = HomeTab.entries

        // Then
        assertThat(tabs).containsExactly(HomeTab.SONGS, HomeTab.LIBRARY).inOrder()
    }

    @Test
    fun `WHEN reading a tab THEN it points at the route of its screen`() {
        // Then
        assertThat(HomeTab.SONGS.route).isEqualTo(SongsRoute)
        assertThat(HomeTab.LIBRARY.route).isEqualTo(LibraryRoute)
    }

    @Test
    fun `WHEN reading the tabs THEN no two of them share a label or an icon`() {
        // When
        val tabs = HomeTab.entries

        // Then
        assertThat(tabs.map { tab -> tab.labelRes }.toSet()).hasSize(tabs.size)
        assertThat(tabs.map { tab -> tab.icon }.toSet()).hasSize(tabs.size)
    }
}
