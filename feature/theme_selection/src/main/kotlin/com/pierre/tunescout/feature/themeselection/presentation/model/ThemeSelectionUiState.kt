package com.pierre.tunescout.feature.themeselection.presentation.model

/**
 * [isDynamicColorEnabled] is null when the device cannot render dynamic color, which is what hides
 * the toggle instead of showing one that would do nothing.
 *
 * @property options the themes the user can pick from.
 * @property isDynamicColorEnabled whether dynamic color is on, or null when the device does not support it.
 */
data class ThemeSelectionUiState(
    val options: List<ThemeOptionUiModel>,
    val isDynamicColorEnabled: Boolean?,
)
