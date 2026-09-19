package com.pierre.tunescout.feature.themeselection.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pierre.tunescout.core.datastore.write
import com.pierre.tunescout.feature.themeselection.data.mapper.ThemePreferenceMapper
import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeSelectionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: ThemePreferenceMapper,
) : ThemeSelectionRepository {
    private val themeKey = stringPreferencesKey("theme")
    private val dynamicColorKey = booleanPreferencesKey("is_dynamic_color_enabled")

    override fun observeTheme(): Flow<Theme> = dataStore.data.map { preferences ->
        mapper.map(preferences[themeKey])
    }

    override suspend fun setTheme(theme: Theme) {
        dataStore.write(key = themeKey, value = mapper.toPreference(theme))
    }

    override fun observeDynamicColorEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[dynamicColorKey] == true
    }

    override suspend fun setDynamicColorEnabled(isEnabled: Boolean) {
        dataStore.write(key = dynamicColorKey, value = isEnabled)
    }
}
