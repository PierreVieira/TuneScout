plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.album"
}

dependencies {
    implementation(projects.core.network.api)
    implementation(projects.core.database.api)
    implementation(projects.core.playback.api)
}
