package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiAction
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumOptionsViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AlbumOptionsScreen(
    route: AlbumOptionsRoute,
    viewModel: AlbumOptionsViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is AlbumOptionsUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        AlbumOptionsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
