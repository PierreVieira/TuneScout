package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.Flow

fun interface ObserveRecentLibrarySearches {
    operator fun invoke(): Flow<List<LibraryItemKey>>
}
