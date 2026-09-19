package com.pierre.tunescout.feature.songoptions.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.songoptions.presentation.content.SongOptionsScreen

fun EntryProviderScope<NavKey>.songOptionsEntry() {
    entry<SongOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { route ->
        SongOptionsScreen(route = route)
    }
}
