package com.pierre.tunescout.feature.themeselection.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.feature.themeselection.presentation.model.DynamicColorInfoUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DynamicColorInfoViewModelTest {
    private lateinit var viewModel: DynamicColorInfoViewModel
    private lateinit var storedDynamicColor: MutableStateFlow<Boolean>
    private lateinit var navigator: Navigator

    @Test
    fun `WHEN observing THEN mirrors the stored preference`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(isDynamicColorEnabled = true)

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state).isTrue()
    }

    @Test
    fun `WHEN toggling from the dialog THEN stores the new value`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(isDynamicColorEnabled = false)

        // When
        viewModel.onEvent(DynamicColorInfoUiEvent.OnDynamicColorToggled(isEnabled = true))
        runCurrent()

        // Then
        assertThat(storedDynamicColor.value).isTrue()
        assertThat(viewModel.uiState.value).isTrue()
    }

    @Test
    fun `WHEN clicking got it THEN closes the dialog`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(isDynamicColorEnabled = false)

        // When
        viewModel.onEvent(DynamicColorInfoUiEvent.OnGotItClicked)

        // Then
        verify { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(isDynamicColorEnabled: Boolean) {
        storedDynamicColor = MutableStateFlow(isDynamicColorEnabled)
        navigator = mockk(relaxUnitFun = true)
        viewModel = DynamicColorInfoViewModel(
            observeDynamicColorEnabled = { storedDynamicColor },
            setDynamicColorEnabled = { isEnabled -> storedDynamicColor.value = isEnabled },
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
