package com.pierre.tunescout.feature.themeselection.domain.usecase.impl

import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveDynamicColorEnabled
import kotlinx.coroutines.flow.Flow

class ObserveDynamicColorEnabledUseCase(
    private val repository: ThemeSelectionRepository,
) : ObserveDynamicColorEnabled {
    override fun invoke(): Flow<Boolean> = repository.observeDynamicColorEnabled()
}
