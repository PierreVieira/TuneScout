plugins {
    alias(libs.plugins.tunescout.android.library.compose)
}

android {
    namespace = "com.pierre.tunescout.screenshotfixtures"
}

// The songs, album and artwork shared by the README generators in :tools:screenshots and the
// screenshot tests in :tools:screenshot_tests. See docs/screenshots.md.
dependencies {
    api(projects.core.model)
    implementation(projects.ui.theme)
    implementation(projects.core.testing)

    // Leaks through pagingItems, which hands a LazyPagingItems to the screen under test
    api(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.common)
    implementation(libs.coil.compose)
    implementation(libs.coil.test)
}
