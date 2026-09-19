package com.pierre.tunescout.feature.songs.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.SongsRoute
import com.pierre.tunescout.feature.songs.presentation.content.SongsScreen

fun EntryProviderScope<NavKey>.songsEntry() {
    entry<SongsRoute> {
        SongsScreen()
    }
}
