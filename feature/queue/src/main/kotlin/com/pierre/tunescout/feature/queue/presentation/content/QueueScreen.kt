package com.pierre.tunescout.feature.queue.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiAction
import com.pierre.tunescout.feature.queue.presentation.viewmodel.QueueViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QueueScreen(viewModel: QueueViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is QueueUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        QueueContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
