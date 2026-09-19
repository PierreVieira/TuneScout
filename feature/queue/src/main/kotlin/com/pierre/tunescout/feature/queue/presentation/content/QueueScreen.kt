package com.pierre.tunescout.feature.queue.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.queue.presentation.viewmodel.QueueViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QueueScreen(viewModel: QueueViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QueueContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
