package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiAction
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AlbumScreen(
    route: AlbumRoute,
    viewModel: AlbumViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is AlbumUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        AlbumContent(
            uiState = uiState,
            isHeaderInline = rememberWindowSize().isSideBySide,
            onEvent = viewModel::onEvent,
        )
    }
}
