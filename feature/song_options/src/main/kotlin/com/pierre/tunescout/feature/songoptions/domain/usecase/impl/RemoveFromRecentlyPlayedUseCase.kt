package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.RemoveFromRecentlyPlayed

internal class RemoveFromRecentlyPlayedUseCase(
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
) : RemoveFromRecentlyPlayed {
    override suspend fun invoke(songId: Long) {
        recentlyPlayedLocalDataSource.remove(songId)
    }
}
