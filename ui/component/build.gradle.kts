plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.ui.component"
}

dependencies {
    implementation(projects.ui.theme)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.coil.compose)
    implementation(libs.compose.shimmer)
    runtimeOnly(libs.coil.network.ktor)
}
