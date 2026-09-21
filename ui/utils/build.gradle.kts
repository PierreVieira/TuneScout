plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.ui.utils"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)

    implementation(libs.androidx.compose.material3.adaptive)

    testImplementation(projects.core.testing)
}
