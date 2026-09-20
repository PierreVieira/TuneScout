package com.pierre.tunescout.presentation.model

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SystemBarUiModelTest {
    @Test
    fun `GIVEN two light bars alike WHEN compared THEN are equal although each builds a style of its own`() {
        // Given
        val first = SystemBarUiModel.Light(scrim = Color.White, darkScrim = Color.Black)
        val second = SystemBarUiModel.Light(scrim = Color.White, darkScrim = Color.Black)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.style).isNotSameInstanceAs(second.style)
    }

    @Test
    fun `GIVEN two dark bars alike WHEN compared THEN are equal although each builds a style of its own`() {
        // Given
        val first = SystemBarUiModel.Dark(scrim = Color.Black)
        val second = SystemBarUiModel.Dark(scrim = Color.Black)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.style).isNotSameInstanceAs(second.style)
    }

    @Test
    fun `GIVEN two bars following the system alike WHEN compared THEN are equal although each builds its own style`() {
        // Given
        val first = SystemBarUiModel.FollowSystem(lightScrim = Color.White, darkScrim = Color.Black)
        val second = SystemBarUiModel.FollowSystem(lightScrim = Color.White, darkScrim = Color.Black)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.style).isNotSameInstanceAs(second.style)
    }
}
