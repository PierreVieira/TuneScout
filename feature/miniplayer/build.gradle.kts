plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.miniplayer"
}

dependencies {
    implementation(projects.core.playback)
}
