plugins {
    alias(libs.plugins.tunescout.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pierre.tunescout.core.database"

    // MigrationTestHelper reads the exported schemas from the test APK's assets.
    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
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

    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.sqlite.bundled)
    androidTestImplementation(libs.androidx.test.core.ktx)
}
