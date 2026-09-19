package com.pierre.tunescout.feature.addtoplaylist.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel.AddToPlaylistViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddToPlaylistScreen(
    route: AddToPlaylistRoute,
    viewModel: AddToPlaylistViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AddToPlaylistContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
