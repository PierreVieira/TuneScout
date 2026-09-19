plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.ui.component"
}

dependencies {
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.compose.shimmer)
    runtimeOnly(libs.coil.network.ktor)
}
