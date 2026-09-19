package com.pierre.tunescout.buildlogic

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The Kover variant CI reports and verifies. It holds one build variant per module, so the merged
 * report runs `testDebugUnitTest` once instead of every Android variant the default report covers.
 */
const val COVERAGE_VARIANT = "ci"

/**
 * Test helpers and build tooling are not production code, and `:ui:*` is composables plus theme
 * constants, which the instrumented tests cover, so they stay out of the coverage report.
 */
private val Project.isMeasuredForCoverage: Boolean
    get() = path != ":core:testing" && !path.startsWith(":tools:") && !path.startsWith(":ui:")

/**
 * Applies Kover and maps [testedVariant] (`debug` on Android, `jvm` on plain Kotlin modules) into
 * [COVERAGE_VARIANT].
 */
fun Project.configureCoverage(testedVariant: String) {
    if (!isMeasuredForCoverage) return
    pluginManager.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<KoverProjectExtension> {
        currentProject {
            createVariant(COVERAGE_VARIANT) {
                add(testedVariant)
            }
        }
    }
}
