plugins {
    alias(libs.plugins.tunescout.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pierre.tunescout.core.navigation"
}

dependencies {
    implementation(projects.ui.utils)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose)
    implementation(libs.kotlinx.serialization.json)
}
