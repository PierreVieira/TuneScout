package com.pierre.tunescout.feature.themeselection.domain.usecase

fun interface IsDynamicColorSupported {
    operator fun invoke(): Boolean
}
