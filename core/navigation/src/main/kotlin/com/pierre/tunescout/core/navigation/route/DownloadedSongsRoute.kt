package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The songs the user downloaded one by one, rather than with an album or a playlist. */
@Serializable
data object DownloadedSongsRoute : NavKey
