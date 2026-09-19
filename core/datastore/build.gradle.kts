plugins {
    alias(libs.plugins.tunescout.android.library)
}

android {
    namespace = "com.pierre.tunescout.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)

    implementation(libs.koin.android)
}
