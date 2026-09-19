package com.pierre.tunescout.feature.themeselection.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.themeselection.presentation.viewmodel.ThemeSelectionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeSelectionScreen(viewModel: ThemeSelectionViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ThemeSelectionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
