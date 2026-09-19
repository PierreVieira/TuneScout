package com.pierre.tunescout.feature.themeselection.domain.usecase.impl

import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.feature.themeselection.domain.usecase.SetTheme
import com.pierre.tunescout.ui.theme.Theme

class SetThemeUseCase(
    private val repository: ThemeSelectionRepository,
) : SetTheme {
    override suspend fun invoke(theme: Theme) {
        repository.setTheme(theme)
    }
}
