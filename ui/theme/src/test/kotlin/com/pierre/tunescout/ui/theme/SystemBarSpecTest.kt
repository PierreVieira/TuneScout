package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SystemBarSpecTest {
    @Test
    fun `GIVEN two light bars alike WHEN compared THEN are equal although each builds a style of its own`() {
        // Given
        val first = SystemBarSpec.Light(scrim = Color.White, darkScrim = Color.Black)
        val second = SystemBarSpec.Light(scrim = Color.White, darkScrim = Color.Black)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.style).isNotSameInstanceAs(second.style)
    }

    @Test
    fun `GIVEN two dark bars alike WHEN compared THEN are equal although each builds a style of its own`() {
        // Given
        val first = SystemBarSpec.Dark(scrim = Color.Black)
        val second = SystemBarSpec.Dark(scrim = Color.Black)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.style).isNotSameInstanceAs(second.style)
    }
}
