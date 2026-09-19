package com.pierre.tunescout.feature.themeselection.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.ThemeSelectionRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.themeselection.presentation.content.ThemeSelectionScreen

fun EntryProviderScope<NavKey>.themeSelectionEntry() {
    entry<ThemeSelectionRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        ThemeSelectionScreen()
    }
}
