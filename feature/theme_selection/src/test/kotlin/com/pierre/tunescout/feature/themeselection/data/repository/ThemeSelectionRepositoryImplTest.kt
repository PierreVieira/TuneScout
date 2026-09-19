package com.pierre.tunescout.feature.themeselection.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.themeselection.data.mapper.ThemePreferenceMapperImpl
import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ThemeSelectionRepositoryImplTest {
    private lateinit var repository: ThemeSelectionRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = ThemeSelectionRepositoryImpl(
            dataStore = FakePreferencesDataStore(),
            mapper = ThemePreferenceMapperImpl(),
        )
    }

    @Test
    fun `GIVEN an empty store WHEN observing THEN falls back to the system theme`() = runTest {
        // When
        val theme = repository.observeTheme().first()

        // Then
        assertThat(theme).isEqualTo(Theme.SYSTEM)
    }

    @Test
    fun `WHEN setting a theme THEN the observed theme follows`() = runTest {
        // When
        repository.setTheme(Theme.LIGHT)

        // Then
        assertThat(repository.observeTheme().first()).isEqualTo(Theme.LIGHT)
    }

    @Test
    fun `GIVEN an empty store WHEN observing dynamic colors THEN they are off`() = runTest {
        // When
        val isEnabled = repository.observeDynamicColorEnabled().first()

        // Then
        assertThat(isEnabled).isFalse()
    }

    @Test
    fun `WHEN enabling dynamic colors THEN the observed value follows`() = runTest {
        // When
        repository.setDynamicColorEnabled(isEnabled = true)

        // Then
        assertThat(repository.observeDynamicColorEnabled().first()).isTrue()
    }

    @Test
    fun `WHEN writing one preference THEN the other keeps its value`() = runTest {
        // Given
        repository.setDynamicColorEnabled(isEnabled = true)

        // When
        repository.setTheme(Theme.DARK)

        // Then
        assertThat(repository.observeDynamicColorEnabled().first()).isTrue()
        assertThat(repository.observeTheme().first()).isEqualTo(Theme.DARK)
    }
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences>
        field = MutableStateFlow<Preferences>(emptyPreferences())

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(data.value).also { updated -> data.value = updated }
}
