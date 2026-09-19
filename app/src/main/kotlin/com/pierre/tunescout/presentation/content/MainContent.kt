package com.pierre.tunescout.presentation.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pierre.tunescout.navigation.TuneScoutNavDisplay
import com.pierre.tunescout.permission.rememberNotificationPermissionRequest
import com.pierre.tunescout.presentation.model.MainUiAction
import com.pierre.tunescout.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
fun MainContent(
    uiAction: Flow<MainUiAction>,
    modifier: Modifier = Modifier,
) {
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    ActionCollector(flow = uiAction) { action ->
        when (action) {
            MainUiAction.RequestNotificationPermission -> requestNotificationPermission()
        }
    }
    Surface(modifier = modifier.fillMaxSize()) {
        TuneScoutNavDisplay()
    }
}
