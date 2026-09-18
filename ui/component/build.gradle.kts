plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.quare.tunescout.ui.component"
}

dependencies {
    implementation(projects.ui.theme)
    implementation(libs.coil.compose)
}
