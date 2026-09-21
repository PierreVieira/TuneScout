plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

// Generates :app's Baseline Profile and measures what it buys. Nothing here runs on CI: both need
// a device, and the numbers only mean something on one that is left alone. See docs/performance.md.
android {
    namespace = "com.pierre.tunescout.baselineprofile"
    compileSdk = libs.versions.android.compileSdk
        .get()
        .toInt()

    defaultConfig {
        // Macrobenchmark needs API 28 to measure frames and to compile the app ahead of time
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk
            .get()
            .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
}

// A debug build of :app is debuggable, which Macrobenchmark refuses to measure. Without this, a local
// `./gradlew connectedDebugAndroidTest` would pick this module up and fail on it.
androidComponents {
    beforeVariants { variant ->
        variant.enable = variant.buildType != "debug"
    }
}

baselineProfile {
    // Runs against whatever device is connected, physical or emulator, instead of a managed one
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
}
