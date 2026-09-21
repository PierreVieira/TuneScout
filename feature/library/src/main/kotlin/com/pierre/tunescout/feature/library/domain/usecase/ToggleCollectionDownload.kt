package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.LibraryItemKey

fun interface ToggleCollectionDownload {
    /** @param isDownloaded whether the collection is downloaded now, which is what the toggle takes back. */
    suspend operator fun invoke(
        key: LibraryItemKey,
        isDownloaded: Boolean,
    )
}
