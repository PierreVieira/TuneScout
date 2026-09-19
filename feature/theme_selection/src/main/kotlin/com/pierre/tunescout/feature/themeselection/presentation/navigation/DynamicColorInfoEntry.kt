package com.pierre.tunescout.feature.themeselection.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.pierre.tunescout.core.navigation.route.DynamicColorInfoRoute
import com.pierre.tunescout.feature.themeselection.presentation.content.DynamicColorInfoScreen

fun EntryProviderScope<NavKey>.dynamicColorInfoEntry() {
    entry<DynamicColorInfoRoute>(metadata = DialogSceneStrategy.dialog()) {
        DynamicColorInfoScreen()
    }
}
