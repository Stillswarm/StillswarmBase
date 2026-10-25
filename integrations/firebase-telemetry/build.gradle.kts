plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
}

android {
    namespace = "com.stillswarm.base.firebase.telemetry"
}

dependencies {
    api(project(":libraries:telemetry"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
