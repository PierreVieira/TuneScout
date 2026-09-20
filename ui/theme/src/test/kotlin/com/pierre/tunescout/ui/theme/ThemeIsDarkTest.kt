package com.pierre.tunescout.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ThemeIsDarkTest {
    @Test
    fun `GIVEN the light theme WHEN the device is dark THEN stays light`() {
        // When
        val isDark = Theme.LIGHT.isDark(isSystemInDarkTheme = true)

        // Then
        assertThat(isDark).isFalse()
    }

    @Test
    fun `GIVEN the dark theme WHEN the device is light THEN stays dark`() {
        // When
        val isDark = Theme.DARK.isDark(isSystemInDarkTheme = false)

        // Then
        assertThat(isDark).isTrue()
    }

    @Test
    fun `GIVEN the system theme WHEN the device is dark THEN follows it`() {
        // When
        val isDark = Theme.SYSTEM.isDark(isSystemInDarkTheme = true)

        // Then
        assertThat(isDark).isTrue()
    }
}
