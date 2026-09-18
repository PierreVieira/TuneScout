package com.quare.tunescout.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * JUnit 6 Jupiter + Truth + MockK for local unit tests. Shared by every module type.
 */
fun Project.addUnitTestDependencies() {
    dependencies {
        add("testImplementation", platform(libs.findLibrary("junit-bom").get()))
        add("testImplementation", libs.findLibrary("junit-jupiter-api").get())
        add("testImplementation", libs.findLibrary("junit-jupiter-params").get())
        add("testImplementation", libs.findLibrary("truth").get())
        add("testImplementation", libs.findLibrary("mockk").get())
        add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        add("testImplementation", libs.findLibrary("turbine").get())
        add("testRuntimeOnly", libs.findLibrary("junit-jupiter-engine").get())
        add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
    }
}

/**
 * JUnit 6 Jupiter on device through the android-junit5 plugin, which wires its own runner once
 * junit-jupiter-api is on the androidTest classpath.
 */
fun Project.addInstrumentedTestDependencies() {
    dependencies {
        add("androidTestImplementation", platform(libs.findLibrary("junit-bom").get()))
        add("androidTestImplementation", libs.findLibrary("junit-jupiter-api").get())
        add("androidTestImplementation", libs.findLibrary("junit-jupiter-params").get())
        add("androidTestImplementation", libs.findLibrary("truth").get())
        add("androidTestImplementation", libs.findLibrary("androidx-test-runner").get())
    }
}
