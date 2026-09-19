package com.pierre.tunescout.feature.themeselection.presentation.model

import com.pierre.tunescout.ui.theme.Theme

sealed interface ThemeSelectionUiEvent {
    data class OnThemeClicked(
        val theme: Theme,
    ) : ThemeSelectionUiEvent

    data class OnDynamicColorToggled(
        val isEnabled: Boolean,
    ) : ThemeSelectionUiEvent

    data object OnDynamicColorInfoClicked : ThemeSelectionUiEvent
}
