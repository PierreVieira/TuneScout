import com.android.build.api.dsl.ApplicationExtension
import com.pierre.tunescout.buildlogic.addInstrumentedTestDependencies
import com.pierre.tunescout.buildlogic.addUnitTestDependencies
import com.pierre.tunescout.buildlogic.configureAndroid
import com.pierre.tunescout.buildlogic.configureCoverage
import com.pierre.tunescout.buildlogic.configureJUnitPlatform
import com.pierre.tunescout.buildlogic.libs
import com.pierre.tunescout.buildlogic.minSdkVersion
import com.pierre.tunescout.buildlogic.requireVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * The single :app module: Compose, Koin and Navigation 3 wiring plus the test stack.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.application")
            apply("org.jetbrains.kotlin.plugin.compose")
            apply("org.jetbrains.kotlin.plugin.serialization")
            apply("de.mannodermaus.android-junit5")
        }

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)

            defaultConfig {
                minSdk = minSdkVersion
                targetSdk = libs.requireVersion("android-targetSdk").toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildFeatures {
                compose = true
            }

            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", platform(libs.findLibrary("koin-bom").get()))

            add("implementation", libs.findLibrary("androidx-core-ktx").get())
            add("implementation", libs.findLibrary("androidx-activity-compose").get())
            add("implementation", libs.findLibrary("androidx-compose-animation").get())
            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())
            add("implementation", libs.findLibrary("androidx-navigation3-ui").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
            add("implementation", libs.findLibrary("koin-android").get())
            add("implementation", libs.findLibrary("koin-androidx-compose").get())
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())

            add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-android").get())
        }
        addUnitTestDependencies()
        addInstrumentedTestDependencies()
        configureJUnitPlatform()
        configureCoverage(testedVariant = "debug")
    }
}
