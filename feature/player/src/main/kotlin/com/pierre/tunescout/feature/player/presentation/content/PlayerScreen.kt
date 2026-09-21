package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiAction
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerScreen(
    route: PlayerRoute,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is PlayerUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        PlayerContent(
            uiState = uiState,
            isSideBySide = rememberWindowSize().isSideBySide,
            onEvent = viewModel::onEvent,
        )
    }
}
