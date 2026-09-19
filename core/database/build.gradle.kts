plugins {
    alias(libs.plugins.tunescout.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pierre.tunescout.core.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    api(projects.core.model)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.koin.android)
    ksp(libs.androidx.room.compiler)
}

dependencies {
    testImplementation(projects.core.testing)
}
