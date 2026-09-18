plugins {
    alias(libs.plugins.tunescout.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quare.tunescout.core.navigation"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.koin.androidx.compose)
    implementation(libs.kotlinx.serialization.json)
}
