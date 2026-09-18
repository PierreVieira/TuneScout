plugins {
    alias(libs.plugins.tunescout.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pierre.tunescout.core.network"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.utils)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)

    testImplementation(libs.ktor.client.mock)
}
