package com.pierre.tunescout.feature.songoptions.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiAction
import com.pierre.tunescout.feature.songoptions.presentation.viewmodel.SongOptionsViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SongOptionsScreen(
    route: SongOptionsRoute,
    viewModel: SongOptionsViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is SongOptionsUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        SongOptionsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
