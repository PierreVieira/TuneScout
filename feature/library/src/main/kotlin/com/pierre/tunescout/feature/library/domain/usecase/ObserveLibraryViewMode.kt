package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import kotlinx.coroutines.flow.Flow

fun interface ObserveLibraryViewMode {
    operator fun invoke(): Flow<LibraryViewMode>
}
