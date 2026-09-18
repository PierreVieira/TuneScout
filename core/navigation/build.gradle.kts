plugins {
    alias(libs.plugins.tunescout.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.quare.tunescout.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
}
