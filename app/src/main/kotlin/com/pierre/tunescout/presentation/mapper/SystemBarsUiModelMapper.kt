package com.pierre.tunescout.presentation.mapper

import androidx.compose.ui.graphics.Color
import com.pierre.tunescout.presentation.model.SystemBarUiModel
import com.pierre.tunescout.presentation.model.SystemBarsUiModel
import com.pierre.tunescout.ui.theme.Theme

private val navigationBarLightScrim = Color(color = 0xE6FFFFFF)
private val navigationBarDarkScrim = Color(color = 0x801B1B1B)

fun Theme.toSystemBarsUiModel(): SystemBarsUiModel = when (this) {
    Theme.LIGHT -> SystemBarsUiModel(
        statusBar = SystemBarUiModel.Light(scrim = Color.Transparent, darkScrim = Color.Transparent),
        navigationBar = SystemBarUiModel.Light(scrim = navigationBarLightScrim, darkScrim = navigationBarDarkScrim),
    )

    Theme.DARK -> SystemBarsUiModel(
        statusBar = SystemBarUiModel.Dark(scrim = Color.Transparent),
        navigationBar = SystemBarUiModel.Dark(scrim = navigationBarDarkScrim),
    )

    Theme.SYSTEM -> SystemBarsUiModel(
        statusBar = SystemBarUiModel.FollowSystem(lightScrim = Color.Transparent, darkScrim = Color.Transparent),
        navigationBar = SystemBarUiModel.FollowSystem(
            lightScrim = navigationBarLightScrim,
            darkScrim = navigationBarDarkScrim,
        ),
    )
}
