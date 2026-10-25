plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
}

android {
    namespace = "com.stillswarm.base.testing"
}

dependencies {
    api(project(":libraries:config"))
    api(project(":libraries:telemetry"))
    api(project(":libraries:billing"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
