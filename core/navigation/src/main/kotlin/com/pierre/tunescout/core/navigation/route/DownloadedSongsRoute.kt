package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

/** The songs the user downloaded one by one, rather than with an album or a playlist. */
@Serializable
data object DownloadedSongsRoute : DetailPaneRoute
