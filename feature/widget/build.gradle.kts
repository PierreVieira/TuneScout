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
    // Glance brings WorkManager 2.7, and with it Room 2.2, whose R8 rule keeps WorkDatabase_Impl but
    // not its constructor: a minified build crashed on start. A newer WorkManager brings a Room
    // that keeps it.
    runtimeOnly(libs.androidx.work.runtime)
    implementation(libs.coil.singleton)
}
