rootProject.name = "TuneScout"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    // Lets Gradle download the JDK pinned in gradle/gradle-daemon-jvm.properties when it is missing
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app")
include(":baselineprofile")

include(":core:model")
include(":core:utils")
include(":core:navigation")
include(":core:network:api")
include(":core:network:impl")
include(":core:database:api")
include(":core:database:impl")
include(":core:datastore")
include(":core:testing")
include(":core:playback:api")
include(":core:playback:impl")

include(":ui:theme")
include(":ui:component")
include(":ui:utils")

include(":feature:splash")
include(":feature:songs")
include(":feature:library")
include(":feature:song_options")
include(":feature:album")
include(":feature:player")
include(":feature:mini_player")
include(":feature:queue")
include(":feature:theme_selection")
include(":feature:widget")
include(":feature:add_to_playlist")

include(":tools:ktlint_custom_rules")
include(":tools:screenshots")
include(":tools:screenshot_fixtures")
include(":tools:screenshot_tests")
