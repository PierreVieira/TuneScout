plugins {
    alias(libs.plugins.tunescout.jvm.library)
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
