package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CollectionScreen(
    key: CollectionKey,
    viewModel: CollectionViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CollectionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
