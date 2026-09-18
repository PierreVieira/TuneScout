package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerScreen(
    route: PlayerRoute,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
