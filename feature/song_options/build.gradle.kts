plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.songoptions"
}

dependencies {
    implementation(projects.core.database.api)
    implementation(projects.core.playback.api)
}
