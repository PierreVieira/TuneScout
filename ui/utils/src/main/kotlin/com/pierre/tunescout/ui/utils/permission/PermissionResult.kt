package com.pierre.tunescout.ui.utils.permission

enum class PermissionResult {
    Granted,

    /** Refused this time; asking again shows the system dialog again. */
    Denied,

    /** Refused for good: the system no longer shows its dialog, only the app's settings can grant it. */
    PermanentlyDenied,
}
