import com.android.build.api.dsl.LibraryExtension
import com.pierre.tunescout.buildlogic.addInstrumentedTestDependencies
import com.pierre.tunescout.buildlogic.addUnitTestDependencies
import com.pierre.tunescout.buildlogic.configureAndroid
import com.pierre.tunescout.buildlogic.configureJUnitPlatform
import com.pierre.tunescout.buildlogic.libs
import com.pierre.tunescout.buildlogic.minSdkVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Base convention for Android modules without UI (network, database, playback, navigation).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("de.mannodermaus.android-junit5")
        }

        extensions.configure<LibraryExtension> {
            configureAndroid(this)

            defaultConfig {
                minSdk = minSdkVersion
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }

        dependencies {
            add("implementation", platform(libs.findLibrary("koin-bom").get()))
            add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
            add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
            add("implementation", libs.findLibrary("koin-core").get())
        }
        addUnitTestDependencies()
        addInstrumentedTestDependencies()
        configureJUnitPlatform()
    }
}
