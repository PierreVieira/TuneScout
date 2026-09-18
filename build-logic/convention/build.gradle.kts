plugins {
    `kotlin-dsl`
}

group = "com.pierre.tunescout.buildlogic"

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.kotlin.serialization.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.android.junit5.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "tunescout.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "tunescout.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "tunescout.android.library.compose"
            implementationClass = "AndroidComposeLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "tunescout.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "tunescout.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
