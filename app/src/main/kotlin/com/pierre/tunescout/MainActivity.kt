package com.pierre.tunescout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.presentation.content.MainContent
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
            setOnExitAnimationListener(SplashScreenViewProvider::remove)
        }
        super.onCreate(savedInstanceState)
        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(isSystemInDarkTheme) { viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme) }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (val state = uiState) {
                MainUiState.Loading -> Unit
                is MainUiState.Ready -> ThemedContent(state)
            }
        }
    }

    @Composable
    private fun ThemedContent(state: MainUiState.Ready) {
        LaunchedEffect(state.systemBars) {
            enableEdgeToEdge(
                statusBarStyle = state.systemBars.statusBar.style,
                navigationBarStyle = state.systemBars.navigationBar.style,
            )
        }
        TuneScoutTheme(
            theme = state.theme,
            isDynamicColorEnabled = state.isDynamicColorEnabled,
        ) {
            MainContent(
                requestNotificationPermissionsUiAction = viewModel.requestNotificationPermissionsUiAction,
                isOffline = state.isOffline,
            )
        }
    }
}
