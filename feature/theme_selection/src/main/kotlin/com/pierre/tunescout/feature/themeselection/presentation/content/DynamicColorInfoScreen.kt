package com.pierre.tunescout.feature.themeselection.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.themeselection.presentation.viewmodel.DynamicColorInfoViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DynamicColorInfoScreen(viewModel: DynamicColorInfoViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DynamicColorInfoContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
