package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class AlbumOptionsRoute(
    val albumId: Long,
) : OverlayRoute
