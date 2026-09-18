plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.quare.tunescout.feature.player"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.playback)
}
