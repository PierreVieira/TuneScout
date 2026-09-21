plugins {
    alias(libs.plugins.tunescout.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(projects.core.playback.api)
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.kotlinx.coroutines.test)
}
