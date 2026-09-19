package com.pierre.tunescout.feature.themeselection.domain.usecase.impl

import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.feature.themeselection.domain.usecase.SetDynamicColorEnabled

class SetDynamicColorEnabledUseCase(
    private val repository: ThemeSelectionRepository,
) : SetDynamicColorEnabled {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setDynamicColorEnabled(isEnabled)
    }
}
