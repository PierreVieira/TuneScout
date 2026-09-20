package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SystemBarsTest {
    private val navigationBarLightScrim = Color(color = 0xE6FFFFFF)
    private val navigationBarDarkScrim = Color(color = 0x801B1B1B)

    @Test
    fun `GIVEN the light palette WHEN built THEN draws dark icons over a light navigation bar`() {
        // When
        val systemBars = SystemBars.of(isDark = false)

        // Then
        assertThat(systemBars).isEqualTo(
            SystemBars(
                statusBar = SystemBarSpec.Light(scrim = Color.Transparent, darkScrim = Color.Transparent),
                navigationBar = SystemBarSpec.Light(
                    scrim = navigationBarLightScrim,
                    darkScrim = navigationBarDarkScrim,
                ),
            ),
        )
    }

    @Test
    fun `GIVEN the dark palette WHEN built THEN draws light icons over a dark navigation bar`() {
        // When
        val systemBars = SystemBars.of(isDark = true)

        // Then
        assertThat(systemBars).isEqualTo(
            SystemBars(
                statusBar = SystemBarSpec.Dark(scrim = Color.Transparent),
                navigationBar = SystemBarSpec.Dark(scrim = navigationBarDarkScrim),
            ),
        )
    }
}
