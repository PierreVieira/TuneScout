package com.quare.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AlbumRoute(
    val albumId: Long,
) : NavKey
