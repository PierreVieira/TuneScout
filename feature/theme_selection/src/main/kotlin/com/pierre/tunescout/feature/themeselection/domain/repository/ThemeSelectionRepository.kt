package com.pierre.tunescout.feature.themeselection.domain.repository

import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeSelectionRepository {
    fun observeTheme(): Flow<Theme>

    suspend fun setTheme(theme: Theme)

    fun observeDynamicColorEnabled(): Flow<Boolean>

    suspend fun setDynamicColorEnabled(isEnabled: Boolean)
}
