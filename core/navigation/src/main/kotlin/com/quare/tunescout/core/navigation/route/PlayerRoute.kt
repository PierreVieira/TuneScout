package com.quare.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class PlayerRoute(
    val songId: Long,
) : NavKey
