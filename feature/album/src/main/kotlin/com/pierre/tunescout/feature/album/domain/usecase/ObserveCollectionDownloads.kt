package com.pierre.tunescout.feature.album.domain.usecase

import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.Flow

fun interface ObserveCollectionDownloads {
    operator fun invoke(): Flow<Set<LibraryItemKey>>
}
