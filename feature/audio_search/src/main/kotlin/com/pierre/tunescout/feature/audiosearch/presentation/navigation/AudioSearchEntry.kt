package com.pierre.tunescout.feature.audiosearch.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.AudioSearchRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.audiosearch.presentation.content.AudioSearchScreen

fun EntryProviderScope<NavKey>.audioSearchEntry() {
    entry<AudioSearchRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        AudioSearchScreen()
    }
}
