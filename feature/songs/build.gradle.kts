plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.songs"
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.database.api)
    implementation(projects.core.playback.api)

    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)

    testImplementation(libs.androidx.paging.testing)
}
