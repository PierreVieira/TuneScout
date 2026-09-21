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
    implementation(libs.coil.singleton)
    implementation(libs.coil.network.ktor)

    implementation(projects.core.model)
    implementation(projects.core.utils)
    implementation(projects.core.navigation)
    implementation(projects.core.network.api)
    implementation(projects.core.network.impl)
    implementation(projects.core.database.api)
    implementation(projects.core.database.impl)
    implementation(projects.core.datastore)
    implementation(projects.core.playback.api)
    implementation(projects.core.playback.impl)
    implementation(projects.ui.theme)
    implementation(projects.ui.component)
    implementation(projects.ui.utils)

    implementation(projects.feature.splash)
    implementation(projects.feature.songs)
    implementation(projects.feature.library)
    implementation(projects.feature.songOptions)
    implementation(projects.feature.miniPlayer)
    implementation(projects.feature.player)
    implementation(projects.feature.queue)
    implementation(projects.feature.album)
    implementation(projects.feature.themeSelection)
    implementation(projects.feature.addToPlaylist)
    implementation(projects.feature.widget)

    testImplementation(libs.koin.test)
    testImplementation(projects.core.testing)
    androidTestImplementation(projects.core.testing)
}
