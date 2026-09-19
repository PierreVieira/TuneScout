package com.pierre.tunescout.feature.themeselection.data.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.ui.theme.Theme
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ThemePreferenceMapperImplTest {
    private lateinit var mapper: ThemePreferenceMapperImpl

    @BeforeEach
    fun setUp() {
        mapper = ThemePreferenceMapperImpl()
    }

    @ParameterizedTest
    @EnumSource(Theme::class)
    fun `WHEN mapping a theme back and forth THEN returns the same theme`(theme: Theme) {
        // When
        val restored = mapper.map(mapper.toPreference(theme))

        // Then
        assertThat(restored).isEqualTo(theme)
    }

    @Test
    fun `GIVEN nothing stored WHEN mapping THEN falls back to the system theme`() {
        // When
        val theme = mapper.map(null)

        // Then
        assertThat(theme).isEqualTo(Theme.SYSTEM)
    }

    @Test
    fun `GIVEN an unknown value WHEN mapping THEN falls back to the system theme`() {
        // When
        val theme = mapper.map("sepia")

        // Then
        assertThat(theme).isEqualTo(Theme.SYSTEM)
    }
}
