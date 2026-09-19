package com.pierre.tunescout.feature.queue.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.queue.presentation.content.QueueScreen

fun EntryProviderScope<NavKey>.queueEntry() {
    entry<QueueRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        QueueScreen()
    }
}
