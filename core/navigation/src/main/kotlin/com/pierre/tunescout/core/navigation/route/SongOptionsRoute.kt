package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class SongOptionsRoute(
    val songId: Long,
) : OverlayRoute
