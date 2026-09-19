package com.pierre.tunescout.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val isDynamicColorSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
