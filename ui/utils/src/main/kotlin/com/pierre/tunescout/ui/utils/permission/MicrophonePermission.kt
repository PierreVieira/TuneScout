package com.pierre.tunescout.ui.utils.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Asks for the microphone only when it is not granted yet, so the caller requests it every time
 * and gets [PermissionResult.Granted] straight away once the user has said yes.
 *
 * @return the function that starts the request; [onResult] receives its outcome.
 */
@Composable
fun rememberMicrophonePermissionRequest(onResult: (PermissionResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        currentOnResult(if (isGranted) PermissionResult.Granted else activity.toDenial())
    }
    return remember(context, launcher) {
        {
            if (context.hasMicrophonePermission()) {
                currentOnResult(PermissionResult.Granted)
            } else {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

/**
 * Read after a refusal, where the rationale flag tells the two denials apart: the system still
 * wants the app to explain itself after a first "no", and stops once it will not ask again.
 *
 * @return which of the two denials this one was. Without an activity to ask, the permanent one.
 */
private fun Activity?.toDenial(): PermissionResult {
    val canAskAgain = this != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
    return if (canAskAgain) PermissionResult.Denied else PermissionResult.PermanentlyDenied
}

private fun Context.hasMicrophonePermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
