package com.pierre.tunescout.feature.themeselection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.domain.usecase.SetDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.presentation.model.DynamicColorInfoUiEvent
import com.pierre.tunescout.feature.themeselection.presentation.model.DynamicColorInfoUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DynamicColorInfoViewModel(
    private val setDynamicColorEnabled: SetDynamicColorEnabled,
    private val navigator: Navigator,
    observeDynamicColorEnabled: ObserveDynamicColorEnabled,
) : ViewModel() {
    val uiState: StateFlow<DynamicColorInfoUiState> = observeDynamicColorEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun onEvent(event: DynamicColorInfoUiEvent) = when (event) {
        is DynamicColorInfoUiEvent.OnDynamicColorToggled -> setEnabled(event.isEnabled)
        DynamicColorInfoUiEvent.OnGotItClicked -> navigator.navigateBack()
    }

    private fun setEnabled(isEnabled: Boolean) {
        viewModelScope.launch { setDynamicColorEnabled(isEnabled) }
    }
}
