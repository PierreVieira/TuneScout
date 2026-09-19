package com.pierre.tunescout.feature.themeselection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.DynamicColorInfoRoute
import com.pierre.tunescout.feature.themeselection.domain.usecase.ThemeSelectionUseCases
import com.pierre.tunescout.feature.themeselection.presentation.mapper.toUiModel
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeSelectionViewModel(
    private val useCases: ThemeSelectionUseCases,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = ThemeSelectionUiState(options = emptyList(), isDynamicColorEnabled = null)

    val uiState: StateFlow<ThemeSelectionUiState> = combine(
        useCases.observeTheme(),
        useCases.observeDynamicColorEnabled(),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: ThemeSelectionUiEvent) = when (event) {
        is ThemeSelectionUiEvent.OnThemeClicked -> launch { useCases.setTheme(event.theme) }
        is ThemeSelectionUiEvent.OnDynamicColorToggled -> launch { useCases.setDynamicColorEnabled(event.isEnabled) }
        ThemeSelectionUiEvent.OnDynamicColorInfoClicked -> navigator.navigate(DynamicColorInfoRoute)
    }

    private fun toUiState(
        theme: Theme,
        isDynamicColorEnabled: Boolean,
    ): ThemeSelectionUiState = ThemeSelectionUiState(
        options = Theme.entries.map { option -> option.toUiModel(selectedTheme = theme) },
        isDynamicColorEnabled = isDynamicColorEnabled.takeIf { useCases.isDynamicColorSupported() },
    )

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
