package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.IsRecentlyPlayed
import kotlinx.coroutines.flow.Flow

internal class IsRecentlyPlayedUseCase(
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
) : IsRecentlyPlayed {
    override fun invoke(songId: Long): Flow<Boolean> = recentlyPlayedLocalDataSource.observeIsRecentlyPlayed(songId)
}
