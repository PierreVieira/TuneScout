plugins {
    alias(libs.plugins.tunescout.android.application)
}

android {
    namespace = "com.quare.tunescout"

    defaultConfig {
        applicationId = "com.quare.tunescout"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.utils)
    implementation(projects.core.navigation)
    implementation(projects.core.designsystem)
}
