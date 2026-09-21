package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.usecase.ObserveCollectionDownloads
import kotlinx.coroutines.flow.Flow

internal class ObserveCollectionDownloadsUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
) : ObserveCollectionDownloads {
    override fun invoke(): Flow<Set<LibraryItemKey>> = downloadLocalDataSource.observeCollections()
}
