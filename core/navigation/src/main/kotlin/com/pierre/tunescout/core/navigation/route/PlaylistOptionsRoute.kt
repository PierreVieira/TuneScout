package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistOptionsRoute(
    val playlistId: Long,
) : OverlayRoute
