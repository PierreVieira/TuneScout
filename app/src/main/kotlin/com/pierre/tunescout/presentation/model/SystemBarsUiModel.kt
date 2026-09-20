package com.pierre.tunescout.presentation.model

/**
 * @property statusBar how the status bar is drawn over the edge-to-edge content.
 * @property navigationBar how the navigation bar is drawn over the edge-to-edge content.
 */
data class SystemBarsUiModel(
    val statusBar: SystemBarUiModel,
    val navigationBar: SystemBarUiModel,
)
