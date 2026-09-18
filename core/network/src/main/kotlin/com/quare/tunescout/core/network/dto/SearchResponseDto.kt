package com.quare.tunescout.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResponseDto(
    val resultCount: Int,
    val results: List<ResultDto>,
)
