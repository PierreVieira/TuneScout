package com.pierre.tunescout.feature.themeselection.domain.usecase

fun interface SetDynamicColorEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
