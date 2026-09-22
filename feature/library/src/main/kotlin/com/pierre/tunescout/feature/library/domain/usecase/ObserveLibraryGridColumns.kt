package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import kotlinx.coroutines.flow.Flow

fun interface ObserveLibraryGridColumns {
    operator fun invoke(): Flow<LibraryGridColumns>
}
