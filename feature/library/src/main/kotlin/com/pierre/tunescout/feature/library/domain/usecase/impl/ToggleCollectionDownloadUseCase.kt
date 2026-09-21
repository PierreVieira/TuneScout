package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.usecase.ToggleCollectionDownload

internal class ToggleCollectionDownloadUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
) : ToggleCollectionDownload {
    override suspend fun invoke(
        key: LibraryItemKey,
        isDownloaded: Boolean,
    ) {
        if (isDownloaded) {
            downloadLocalDataSource.removeCollection(key)
        } else {
            downloadLocalDataSource.addCollection(key)
        }
    }
}
