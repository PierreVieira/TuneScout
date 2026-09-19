package com.pierre.tunescout.feature.library.domain.usecase

fun interface CreatePlaylist {
    suspend operator fun invoke(name: String): Long
}
