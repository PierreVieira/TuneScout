plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.audiosearch"
}

dependencies {
    implementation(projects.core.audioSearch)
}
