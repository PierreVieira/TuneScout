import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.jraska.module.graph.assertion)
}

val ktlintVersion: String = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")
    .findVersion("ktlint")
    .get()
    .requiredVersion

moduleGraphAssert {
    // Only production dependencies count as architecture: a feature's tests may reach core:testing
    configurations = setOf("api", "implementation")
    // :app is the composition root and the only module allowed to see features.
    // Features never see each other: they talk through :core:navigation routes and shared
    // :core:* state. :ui:* is presentation-only and knows nothing about features or data.
    // :core:navigation is the one core module that may reach :ui (its command collector is a
    // composable). Nothing depends on :app.
    restricted = arrayOf(
        ":feature:.* -X> :feature:.*",
        ":core:.* -X> :feature:.*",
        ":ui:.* -X> :feature:.*",
        ":ui:.* -X> :core:.*",
        ":core:(?!navigation).* -X> :ui:.*",
        ".* -X> :app",
    )
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    if (path != ":tools:ktlint-custom-rules") {
        dependencies {
            add("ktlintRuleset", project(":tools:ktlint-custom-rules"))
        }
    }

    extensions.configure<KtlintExtension> {
        version.set(ktlintVersion)
        debug.set(false)
        verbose.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)

        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            include("**/kotlin/**")
        }

        reporters {
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}
