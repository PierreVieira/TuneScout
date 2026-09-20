plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.ui.theme"
}

dependencies {
    implementation(libs.androidx.activity)
}
