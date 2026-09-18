plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.screenshots"
}

dependencies {
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(projects.core.model)
    testImplementation(projects.core.testing)
    testImplementation(projects.feature.album)
    testImplementation(projects.feature.player)
    testImplementation(projects.feature.songs)
    testImplementation(projects.feature.splash)
    testImplementation(projects.ui.theme)

    testImplementation(libs.androidx.paging.common)
    testImplementation(libs.androidx.paging.compose)
    testImplementation(libs.coil.compose)
    testImplementation(libs.coil.test)
    testImplementation(libs.store.screenshots)
    testRuntimeOnly(libs.junit.vintage.engine)
}

// The generators are JUnit 4 (Robolectric) in a project that is otherwise JUnit 6, so this is the
// one module with the vintage engine on its test runtime. See docs/screenshots.md.
tasks.withType<Test>().configureEach {
    val generatedDir = rootProject.layout.buildDirectory.dir("outputs/screenshots")
    systemProperty("storeScreenshots.outputRoot", generatedDir.get().asFile.absolutePath)
    systemProperty("roborazzi.test.record", "true")
    outputs.dir(generatedDir)
}
