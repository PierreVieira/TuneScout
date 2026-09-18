package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SongOptionsRoute(
    val songId: Long,
) : NavKey
