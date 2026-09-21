package com.pierre.tunescout.presentation.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.pierre.tunescout.navigation.TuneScoutNavigationContent
import com.pierre.tunescout.permission.rememberNotificationPermissionRequest
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.network.LocalIsOffline
import kotlinx.coroutines.flow.Flow

@Composable
fun MainContent(
    requestNotificationPermissionsUiAction: Flow<Unit>,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
) {
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    ActionCollector(flow = requestNotificationPermissionsUiAction) {
        requestNotificationPermission()
    }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .exposeTestTagsToUiAutomator(),
    ) {
        CompositionLocalProvider(LocalIsOffline provides isOffline) {
            TuneScoutNavigationContent()
        }
    }
}

/**
 * UiAutomator only sees what reaches the accessibility tree as a resource id, and a Compose
 * `testTag` does not unless this is set above it. The macrobenchmarks in :baselineprofile drive a
 * release build through UiAutomator, so this is on in every build type.
 *
 * @return this modifier, with the test tags below it visible to UiAutomator.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.exposeTestTagsToUiAutomator(): Modifier = semantics { testTagsAsResourceId = true }
