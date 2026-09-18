plugins {
    alias(libs.plugins.tunescout.android.library)
}

android {
    namespace = "com.quare.tunescout.core.playback"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.utils)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.koin.android)

    testImplementation(projects.core.testing)
}
