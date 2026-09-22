package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

/** The songs the user downloaded one by one, the latest first. */
fun interface ObserveDownloadedSongs {
    operator fun invoke(): Flow<List<Song>>
}
