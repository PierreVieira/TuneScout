package com.pierre.tunescout.feature.themeselection.presentation.mapper

import androidx.compose.ui.graphics.vector.ImageVector
import com.pierre.tunescout.feature.themeselection.R
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeOptionUiModel
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.Theme

fun Theme.toUiModel(selectedTheme: Theme): ThemeOptionUiModel = ThemeOptionUiModel(
    theme = this,
    titleRes = toTitleRes(),
    icon = toIcon(),
    isSelected = this == selectedTheme,
)

private fun Theme.toTitleRes(): Int = when (this) {
    Theme.LIGHT -> R.string.theme_selection_light
    Theme.DARK -> R.string.theme_selection_dark
    Theme.SYSTEM -> R.string.theme_selection_system
}

private fun Theme.toIcon(): ImageVector = when (this) {
    Theme.LIGHT -> TuneScoutIcons.lightTheme
    Theme.DARK -> TuneScoutIcons.darkTheme
    Theme.SYSTEM -> TuneScoutIcons.systemTheme
}
