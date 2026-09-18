plugins {
    alias(libs.plugins.tunescout.android.library)
}

android {
    namespace = "com.quare.tunescout.core.playback"
}

dependencies {
    api(projects.core.model)
}
