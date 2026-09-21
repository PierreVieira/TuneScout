plugins {
    alias(libs.plugins.tunescout.android.feature)
}

android {
    namespace = "com.pierre.tunescout.feature.widget"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.core.playback.api)
    implementation(projects.core.database.api)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.coil.singleton)
}
