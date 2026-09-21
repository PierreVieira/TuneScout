package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiAction
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CollectionScreen(
    key: CollectionKey,
    viewModel: CollectionViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is CollectionUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        CollectionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
