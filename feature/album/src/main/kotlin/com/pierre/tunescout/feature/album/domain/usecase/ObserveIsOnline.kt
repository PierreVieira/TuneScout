package com.pierre.tunescout.feature.album.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveIsOnline {
    operator fun invoke(): Flow<Boolean>
}
