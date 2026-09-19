package com.pierre.tunescout.feature.themeselection.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.DynamicColorInfoRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.feature.themeselection.domain.usecase.ThemeSelectionUseCases
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.pierre.tunescout.ui.theme.Theme
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ThemeSelectionViewModelTest {
    private lateinit var viewModel: ThemeSelectionViewModel
    private lateinit var storedTheme: MutableStateFlow<Theme>
    private lateinit var storedDynamicColor: MutableStateFlow<Boolean>
    private lateinit var navigator: Navigator

    @Test
    fun `WHEN observing THEN offers every theme with the stored one selected`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(theme = Theme.DARK)

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.options.map { option -> option.theme })
            .containsExactly(Theme.LIGHT, Theme.DARK, Theme.SYSTEM)
            .inOrder()
        assertThat(state.options.filter { option -> option.isSelected }.map { option -> option.theme })
            .containsExactly(Theme.DARK)
    }

    @Test
    fun `WHEN clicking a theme THEN stores it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(theme = Theme.SYSTEM)

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.OnThemeClicked(Theme.LIGHT))
        runCurrent()

        // Then
        assertThat(storedTheme.value).isEqualTo(Theme.LIGHT)
        assertThat(
            viewModel.uiState.value.options
                .single { option -> option.isSelected }
                .theme,
        ).isEqualTo(Theme.LIGHT)
    }

    @Test
    fun `WHEN toggling dynamic colors THEN stores the new value`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(theme = Theme.SYSTEM, isDynamicColorSupported = true)

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.OnDynamicColorToggled(isEnabled = true))
        runCurrent()

        // Then
        assertThat(storedDynamicColor.value).isTrue()
        assertThat(viewModel.uiState.value.isDynamicColorEnabled).isTrue()
    }

    @Test
    fun `GIVEN a device without dynamic colors WHEN observing THEN hides the toggle`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(theme = Theme.SYSTEM, isDynamicColorSupported = false, isDynamicColorEnabled = true)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.isDynamicColorEnabled).isNull()
        }

    @Test
    fun `WHEN clicking the dynamic colors info THEN opens the explanation dialog`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(theme = Theme.SYSTEM)

            // When
            viewModel.onEvent(ThemeSelectionUiEvent.OnDynamicColorInfoClicked)

            // Then
            verify { navigator.navigate(DynamicColorInfoRoute) }
        }

    private fun TestScope.prepareScenario(
        theme: Theme,
        isDynamicColorSupported: Boolean = true,
        isDynamicColorEnabled: Boolean = false,
    ) {
        storedTheme = MutableStateFlow(theme)
        storedDynamicColor = MutableStateFlow(isDynamicColorEnabled)
        navigator = mockk(relaxUnitFun = true)
        viewModel = ThemeSelectionViewModel(
            useCases = ThemeSelectionUseCases(
                observeTheme = { storedTheme },
                setTheme = { newTheme -> storedTheme.value = newTheme },
                observeDynamicColorEnabled = { storedDynamicColor },
                setDynamicColorEnabled = { isEnabled -> storedDynamicColor.value = isEnabled },
                isDynamicColorSupported = { isDynamicColorSupported },
            ),
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
