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

include(":core:model")
include(":core:utils")
include(":core:navigation")
include(":core:network")
include(":core:database")
include(":core:testing")

include(":ui:theme")
include(":ui:component")
include(":ui:utils")

include(":tools:ktlint-custom-rules")
