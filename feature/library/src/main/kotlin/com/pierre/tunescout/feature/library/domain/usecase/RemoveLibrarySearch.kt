package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.LibraryItemKey

fun interface RemoveLibrarySearch {
    suspend operator fun invoke(key: LibraryItemKey)
}
