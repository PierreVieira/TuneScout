package com.pierre.tunescout.presentation.mapper

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.presentation.model.SystemBarUiModel
import com.pierre.tunescout.presentation.model.SystemBarsUiModel
import com.pierre.tunescout.ui.theme.Theme
import org.junit.jupiter.api.Test

class SystemBarsUiModelMapperTest {
    private val navigationBarLightScrim = Color(color = 0xE6FFFFFF)
    private val navigationBarDarkScrim = Color(color = 0x801B1B1B)

    @Test
    fun `GIVEN the light theme WHEN mapped THEN draws dark icons over a light navigation bar`() {
        // When
        val systemBars = Theme.LIGHT.toSystemBarsUiModel()

        // Then
        assertThat(systemBars).isEqualTo(
            SystemBarsUiModel(
                statusBar = SystemBarUiModel.Light(scrim = Color.Transparent, darkScrim = Color.Transparent),
                navigationBar = SystemBarUiModel.Light(
                    scrim = navigationBarLightScrim,
                    darkScrim = navigationBarDarkScrim,
                ),
            ),
        )
    }

    @Test
    fun `GIVEN the dark theme WHEN mapped THEN draws light icons over a dark navigation bar`() {
        // When
        val systemBars = Theme.DARK.toSystemBarsUiModel()

        // Then
        assertThat(systemBars).isEqualTo(
            SystemBarsUiModel(
                statusBar = SystemBarUiModel.Dark(scrim = Color.Transparent),
                navigationBar = SystemBarUiModel.Dark(scrim = navigationBarDarkScrim),
            ),
        )
    }

    @Test
    fun `GIVEN the system theme WHEN mapped THEN leaves the icons to the device with the scrims of both themes`() {
        // When
        val systemBars = Theme.SYSTEM.toSystemBarsUiModel()

        // Then
        assertThat(systemBars).isEqualTo(
            SystemBarsUiModel(
                statusBar = SystemBarUiModel.FollowSystem(
                    lightScrim = Color.Transparent,
                    darkScrim = Color.Transparent,
                ),
                navigationBar = SystemBarUiModel.FollowSystem(
                    lightScrim = navigationBarLightScrim,
                    darkScrim = navigationBarDarkScrim,
                ),
            ),
        )
    }
}
