package com.pierre.tunescout.feature.themeselection.presentation.model

sealed interface DynamicColorInfoUiEvent {
    data class OnDynamicColorToggled(
        val isEnabled: Boolean,
    ) : DynamicColorInfoUiEvent

    data object OnGotItClicked : DynamicColorInfoUiEvent
}
