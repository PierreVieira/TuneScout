package com.pierre.tunescout

import android.content.Intent
import android.content.res.Configuration
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
        reportLaunchDeepLink(savedInstanceState)
        reportSystemDarkTheme()
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

    private fun reportLaunchDeepLink(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) viewModel.onDeepLinkReceived(intent.dataString)
    }

    /**
     * The splash holds the first frame until the state is ready, so a state that learned the
     * device's dark mode only from the composition would wait for a frame that waits for it. The
     * effect in `setContent` carries the changes that come after this one.
     */
    private fun reportSystemDarkTheme() {
        viewModel.onSystemDarkThemeChanged(resources.configuration.isSystemInDarkTheme)
    }

    /**
     * The activity is `singleTop`, so a widget tapped while the app is already open lands here
     * instead of on a second instance.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.onDeepLinkReceived(intent.dataString)
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

/** Whether the device is in dark mode, as the resources this activity was created with report it. */
private val Configuration.isSystemInDarkTheme: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
