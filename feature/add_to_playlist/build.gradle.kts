plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.addtoplaylist"
}

dependencies {
    implementation(projects.core.database)
}
