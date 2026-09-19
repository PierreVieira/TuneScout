package com.pierre.tunescout.navigation.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.R
import com.pierre.tunescout.core.navigation.route.LibraryRoute
import com.pierre.tunescout.core.navigation.route.SongsRoute
import com.pierre.tunescout.ui.component.TuneScoutIcons

internal enum class HomeTab(
    val route: NavKey,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    SONGS(route = SongsRoute, labelRes = R.string.home_tab_songs, icon = TuneScoutIcons.home),
    LIBRARY(route = LibraryRoute, labelRes = R.string.home_tab_library, icon = TuneScoutIcons.library),
}
