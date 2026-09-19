plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.themeselection"
}

dependencies {
    implementation(projects.core.datastore)
}
