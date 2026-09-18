import com.pierre.tunescout.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention for :feature:* modules. Every feature owns its data/domain/presentation layers and
 * gets the same shared core modules, ViewModel, Navigation 3 and Koin wiring, so its own
 * build.gradle.kts only declares what is specific to it.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("tunescout.android.library.compose")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:utils"))
            add("implementation", project(":core:navigation"))
            add("implementation", project(":ui:theme"))
            add("implementation", project(":ui:component"))
            add("implementation", project(":ui:utils"))
            add("testImplementation", project(":core:testing"))
            add("androidTestImplementation", project(":core:testing"))

            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())
            add("implementation", libs.findLibrary("androidx-navigation3-ui").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
            add("implementation", libs.findLibrary("koin-android").get())
            add("implementation", libs.findLibrary("koin-androidx-compose").get())
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            add("implementation", libs.findLibrary("coil-compose").get())
        }
    }
}
