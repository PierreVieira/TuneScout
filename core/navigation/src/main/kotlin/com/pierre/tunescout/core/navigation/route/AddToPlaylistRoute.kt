package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class AddToPlaylistRoute(
    val songId: Long,
) : OverlayRoute
