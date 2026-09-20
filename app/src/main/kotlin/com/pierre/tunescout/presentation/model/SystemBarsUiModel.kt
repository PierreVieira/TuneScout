package com.pierre.tunescout.presentation.model

import androidx.activity.SystemBarStyle

/**
 * [SystemBarStyle] has no `equals`, so two styles built alike still differ. Each theme maps to one
 * shared instance of this model, which is what keeps [MainUiState.Ready] comparable.
 *
 * @property statusBarStyle how the status bar is drawn over the edge-to-edge content.
 * @property navigationBarStyle how the navigation bar is drawn over the edge-to-edge content.
 */
data class SystemBarsUiModel(
    val statusBarStyle: SystemBarStyle,
    val navigationBarStyle: SystemBarStyle,
)
