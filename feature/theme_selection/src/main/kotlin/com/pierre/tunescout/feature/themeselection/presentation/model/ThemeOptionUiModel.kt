package com.pierre.tunescout.feature.themeselection.presentation.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.pierre.tunescout.ui.theme.Theme

data class ThemeOptionUiModel(
    val theme: Theme,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val isSelected: Boolean,
)
