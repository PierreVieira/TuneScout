package com.pierre.tunescout.presentation.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pierre.tunescout.navigation.TuneScoutNavDisplay
import com.pierre.tunescout.permission.playbackNotificationPermissionRequest
import com.pierre.tunescout.presentation.model.MainUiAction
import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import com.pierre.tunescout.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
) {
    val requestNotificationPermission = playbackNotificationPermissionRequest()
    ActionCollector(flow = viewModel.uiAction) { action ->
        when (action) {
            MainUiAction.RequestNotificationPermission -> requestNotificationPermission()
        }
    }
    Surface(modifier = modifier.fillMaxSize()) {
        TuneScoutNavDisplay()
    }
}
