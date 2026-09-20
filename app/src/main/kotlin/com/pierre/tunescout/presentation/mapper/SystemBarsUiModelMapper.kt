package com.pierre.tunescout.presentation.mapper

import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.pierre.tunescout.presentation.model.SystemBarsUiModel
import com.pierre.tunescout.ui.theme.Theme

private val transparentScrim = Color.Transparent.toArgb()
private val navigationBarLightScrim = Color(color = 0xE6FFFFFF).toArgb()
private val navigationBarDarkScrim = Color(color = 0x801B1B1B).toArgb()
private val lightSystemBars = SystemBarsUiModel(
    statusBarStyle = SystemBarStyle.light(transparentScrim, transparentScrim),
    navigationBarStyle = SystemBarStyle.light(navigationBarLightScrim, navigationBarDarkScrim),
)
private val darkSystemBars = SystemBarsUiModel(
    statusBarStyle = SystemBarStyle.dark(transparentScrim),
    navigationBarStyle = SystemBarStyle.dark(navigationBarDarkScrim),
)
private val followSystemBars = SystemBarsUiModel(
    statusBarStyle = SystemBarStyle.auto(transparentScrim, transparentScrim),
    navigationBarStyle = SystemBarStyle.auto(navigationBarLightScrim, navigationBarDarkScrim),
)

/**
 * Whether the device is in dark mode is a configuration value the ViewModel cannot see, so
 * [Theme.SYSTEM] leaves that choice to [SystemBarStyle.auto], resolved when the styles are applied.
 * A dark mode switch recreates the activity, which applies them again.
 *
 * @return the same instance on every call for a given theme.
 */
fun Theme.toSystemBarsUiModel(): SystemBarsUiModel = when (this) {
    Theme.LIGHT -> lightSystemBars
    Theme.DARK -> darkSystemBars
    Theme.SYSTEM -> followSystemBars
}
