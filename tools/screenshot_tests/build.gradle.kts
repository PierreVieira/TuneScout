plugins {
    alias(libs.plugins.tunescout.android.library.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.pierre.tunescout.screenshottests"
}

dependencies {
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(projects.core.model)
    testImplementation(projects.feature.addToPlaylist)
    testImplementation(projects.feature.album)
    testImplementation(projects.feature.audioSearch)
    testImplementation(projects.feature.library)
    testImplementation(projects.feature.miniPlayer)
    testImplementation(projects.feature.player)
    testImplementation(projects.feature.queue)
    testImplementation(projects.feature.songs)
    testImplementation(projects.feature.themeSelection)
    testImplementation(projects.tools.screenshotFixtures)
    testImplementation(projects.ui.component)
    testImplementation(projects.ui.theme)

    testImplementation(libs.androidx.compose.material3)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.coil.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.accessibility.check)
    testImplementation(libs.roborazzi.compose)
    testRuntimeOnly(libs.junit.vintage.engine)
}

// Robolectric (JUnit 4) in a project that is otherwise JUnit 6, like :tools:screenshots.
tasks.withType<Test>().configureEach {
    maxHeapSize = "4g"
    // Android 16's shared memory reaches FileDescriptor's private fields through jdk.internal,
    // which the JDK only hands to a test that asks for it.
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
    // Half of the 1078x2399 the device renders: the images are versioned, and a layout, a colour or
    // a truncated string is as visible at 539 pixels wide as at 1078, in a quarter of the bytes.
    systemProperty("roborazzi.record.resizeScale", "0.5")
}
