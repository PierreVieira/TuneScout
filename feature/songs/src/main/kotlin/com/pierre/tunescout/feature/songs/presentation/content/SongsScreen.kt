package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiAction
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.viewmodel.SongsViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.permission.openAppSettings
import com.pierre.tunescout.ui.utils.permission.rememberMicrophonePermissionRequest
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SongsScreen(viewModel: SongsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    val requestMicrophonePermission = rememberMicrophonePermissionRequest { result ->
        viewModel.onEvent(SongsUiEvent.OnMicrophonePermissionResult(result))
    }
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is SongsUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))

            SongsUiAction.RequestMicrophonePermission -> requestMicrophonePermission()

            SongsUiAction.ShowMicrophoneSettingsSnackBar -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.songs_microphone_denied_permanently),
                    actionLabel = resources.getString(R.string.songs_open_settings),
                )
                if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
            }
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        SongsContent(
            uiState = uiState,
            searchResults = searchResults,
            isHeaderInline = rememberWindowSize().isSideBySide,
            onEvent = viewModel::onEvent,
        )
    }
}
