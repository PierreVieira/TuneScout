package com.pierre.tunescout.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

private val accessibilityLintChecks = setOf(
    "ContentDescription",
    "LabelFor",
    "ClickableViewAccessibility",
    "KeyboardInaccessibleWidget",
    "GetContentDescriptionOverride",
)

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.requireVersion(alias: String): String = findVersion(alias).get().requiredVersion

val Project.minSdkVersion: Int
    get() = libs.requireVersion("android-minSdk").toInt()

/**
 * A library only gets an androidTest component, and the dependencies that go with it, when it has
 * instrumented tests. Otherwise it compiles, packages and installs an empty test APK, and AGP warns
 * about androidTest dependencies declared on a component that does not exist.
 */
val Project.hasInstrumentedTests: Boolean
    get() = projectDir.resolve("src/androidTest").exists()

/**
 * SDK level and Java target shared by every Android module. The version catalog is the single
 * source of truth so a bump is a one-line change.
 */
fun Project.configureAndroid(extension: CommonExtension) {
    extension.compileSdk = libs.requireVersion("android-compileSdk").toInt()
    extension.compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    extension.compileOptions.targetCompatibility = JavaVersion.VERSION_17
    configureLint(extension)
}

/**
 * Android lint, which nothing ran before. Its accessibility checks are promoted from warnings to
 * errors, so a missing content description or an unlabelled field in a layout — the widget picker's
 * previews are plain XML — fails the build instead of scrolling past in a report nobody opens.
 *
 * `:app` lints its dependencies too, so `./gradlew :app:lintDebug` is the one task that covers every
 * module; see docs/code-quality.md.
 */
private fun configureLint(extension: CommonExtension) {
    extension.lint.abortOnError = true
    extension.lint.checkReleaseBuilds = false
    extension.lint.error += accessibilityLintChecks
}
