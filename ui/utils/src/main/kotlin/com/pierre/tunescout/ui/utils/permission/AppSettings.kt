package com.pierre.tunescout.ui.utils.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Opens this app's page in the system settings, the only place a permanently denied permission can be granted. */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
