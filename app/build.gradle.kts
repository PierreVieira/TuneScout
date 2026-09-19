plugins {
    alias(libs.plugins.tunescout.android.application)
}

android {
    namespace = "com.pierre.tunescout"

    defaultConfig {
        applicationId = "com.pierre.tunescout"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.splashscreen)

    implementation(projects.core.model)
    implementation(projects.core.utils)
    implementation(projects.core.navigation)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.playback)
    implementation(projects.ui.theme)
    implementation(projects.ui.component)
    implementation(projects.ui.utils)

    implementation(projects.feature.splash)
    implementation(projects.feature.songs)
    implementation(projects.feature.miniplayer)
    implementation(projects.feature.player)
    implementation(projects.feature.queue)
    implementation(projects.feature.album)

    testImplementation(libs.koin.test)
    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
}
