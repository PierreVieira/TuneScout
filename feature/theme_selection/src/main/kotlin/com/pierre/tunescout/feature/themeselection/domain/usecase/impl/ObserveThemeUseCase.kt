package com.pierre.tunescout.feature.themeselection.domain.usecase.impl

import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveTheme
import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.Flow

class ObserveThemeUseCase(
    private val repository: ThemeSelectionRepository,
) : ObserveTheme {
    override fun invoke(): Flow<Theme> = repository.observeTheme()
}
