package com.pierre.tunescout.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Settings shared by every module that runs the Compose compiler.
 *
 * - The stability file at the root tells the compiler which types from outside Compose modules are
 *   immutable, so composables taking them can skip. See docs/performance.md.
 * - `-Ptunescout.composeReports=true` writes the compiler's metrics and reports (which classes are
 *   stable, which composables are skippable) to each module's `build/compose_compiler`. They are
 *   off by default because they slow the build and nothing reads them unless someone asks.
 */
fun Project.configureComposeCompiler() {
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))

        val reportsEnabled = providers.gradleProperty("tunescout.composeReports").map(String::toBoolean)
        if (reportsEnabled.getOrElse(false)) {
            val destination = layout.buildDirectory.dir("compose_compiler")
            reportsDestination.set(destination)
            metricsDestination.set(destination)
        }
    }
}
