package com.pierre.tunescout.feature.themeselection.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveDynamicColorEnabled {
    operator fun invoke(): Flow<Boolean>
}
