package com.pierre.tunescout.feature.themeselection.domain.usecase

data class ThemeSelectionUseCases(
    val observeTheme: ObserveTheme,
    val setTheme: SetTheme,
    val observeDynamicColorEnabled: ObserveDynamicColorEnabled,
    val setDynamicColorEnabled: SetDynamicColorEnabled,
    val isDynamicColorSupported: IsDynamicColorSupported,
)
