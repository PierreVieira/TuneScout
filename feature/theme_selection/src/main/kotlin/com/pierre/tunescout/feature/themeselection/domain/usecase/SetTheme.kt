package com.pierre.tunescout.feature.themeselection.domain.usecase

import com.pierre.tunescout.ui.theme.Theme

fun interface SetTheme {
    suspend operator fun invoke(theme: Theme)
}
