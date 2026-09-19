plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.ui.utils"
}

dependencies {
    implementation(libs.androidx.compose.material3.adaptive)
}
