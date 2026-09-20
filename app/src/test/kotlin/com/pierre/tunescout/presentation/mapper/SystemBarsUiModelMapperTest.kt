package com.pierre.tunescout.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.ui.theme.Theme
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SystemBarsUiModelMapperTest {
    @ParameterizedTest
    @EnumSource(Theme::class)
    fun `GIVEN a theme WHEN mapped twice THEN returns the same system bars both times`(theme: Theme) {
        // When
        val first = theme.toSystemBarsUiModel()
        val second = theme.toSystemBarsUiModel()

        // Then
        assertThat(second).isEqualTo(first)
    }

    @Test
    fun `WHEN every theme is mapped THEN each gets system bars of its own`() {
        // When
        val systemBars = Theme.entries.map { theme -> theme.toSystemBarsUiModel() }

        // Then
        assertThat(systemBars).containsNoDuplicates()
    }
}
