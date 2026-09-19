plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.queue"
}

dependencies {
    implementation(projects.core.playback.api)
    implementation(libs.reorderable)
}
