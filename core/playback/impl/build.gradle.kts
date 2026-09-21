plugins {
    alias(libs.plugins.tunescout.android.library)
}

android {
    namespace = "com.pierre.tunescout.core.playback"
}

dependencies {
    implementation(projects.core.playback.api)
    implementation(projects.core.database.api)
    implementation(projects.core.network.api)
    implementation(projects.core.utils)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.koin.android)

    testImplementation(projects.core.testing)
}
