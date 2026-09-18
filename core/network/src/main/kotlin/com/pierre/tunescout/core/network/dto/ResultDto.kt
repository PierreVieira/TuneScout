package com.pierre.tunescout.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ResultDto(
    val wrapperType: String? = null,
    val kind: String? = null,
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionId: Long? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null,
    val trackTimeMillis: Long? = null,
    val trackNumber: Int? = null,
)
