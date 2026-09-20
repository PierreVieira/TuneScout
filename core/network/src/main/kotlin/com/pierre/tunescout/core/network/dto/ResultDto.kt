package com.pierre.tunescout.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ResultDto(
    @SerialName("wrapperType") val wrapperType: String?,
    @SerialName("kind") val kind: String?,
    @SerialName("trackId") val trackId: Long?,
    @SerialName("trackName") val trackName: String?,
    @SerialName("artistName") val artistName: String?,
    @SerialName("collectionId") val collectionId: Long?,
    @SerialName("collectionName") val collectionName: String?,
    @SerialName("artworkUrl100") val artworkUrl100: String?,
    @SerialName("previewUrl") val previewUrl: String?,
    @SerialName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerialName("trackNumber") val trackNumber: Int?,
)
