package com.pierre.tunescout.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResponseDto(
    @SerialName("resultCount") val resultCount: Int,
    @SerialName("results") val results: List<ResultDto>,
)
