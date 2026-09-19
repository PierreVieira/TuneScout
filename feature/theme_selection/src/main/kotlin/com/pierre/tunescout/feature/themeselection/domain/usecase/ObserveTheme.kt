package com.pierre.tunescout.feature.themeselection.domain.usecase

import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.Flow

fun interface ObserveTheme {
    operator fun invoke(): Flow<Theme>
}
