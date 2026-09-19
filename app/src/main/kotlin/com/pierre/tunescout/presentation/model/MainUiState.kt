package com.pierre.tunescout.presentation.model

import com.pierre.tunescout.ui.theme.Theme

/**
 * [Loading] is how long the stored theme takes to arrive. The system splash stays on screen until
 * it ends, so the first frame the user sees is already drawn in the theme they chose.
 */
sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(
        val theme: Theme,
        val isDynamicColorEnabled: Boolean,
    ) : MainUiState
}
